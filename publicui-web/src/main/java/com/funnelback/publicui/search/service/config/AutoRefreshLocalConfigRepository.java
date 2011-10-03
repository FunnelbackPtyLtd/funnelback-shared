package com.funnelback.publicui.search.service.config;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.collection.Profile;

/**
 * Implementation of {@link AbstractLocalConfigRepository} that caches config
 * data and will auto-refresh its cache if any of the underlying files has changed.
 */
@Repository("configRepository")
@CommonsLog
public class AutoRefreshLocalConfigRepository extends CachedLocalConfigRepository {

	@Value("#{appProperties['config.repository.autorefresh.interval']}")
	private int checkingInterval = 0;
		
	/**
	 * Keep track of recent stale checks to avoid checking all
	 * the files to often
	 */
	private Map<String, Long> staleChecks = Collections.synchronizedMap(new HashMap<String, Long>());
	
	/**
	 * Keep track of files that existed and files that were deleted.
	 */
	private Map<String, Boolean> existingFiles = Collections.synchronizedMap(new HashMap<String, Boolean>());
	
	@Override
	public Collection getCollection(String collectionId) {
		Cache cache = appCacheManager.getCache(CACHE);
		Element elt = cache.get(collectionId);
		
		if (elt == null) {
			// Collection never loaded
			log.debug("Collection '"+collectionId+"' not found in cache.");
			return super.getCollection(collectionId); 
		} else {
			log.debug("Collection '"+collectionId+"' found in cache.");
			// Collection in cache
			Collection c = (Collection) elt.getObjectValue();
			
			if(isAnyConfigFileStale(c, elt.getCreationTime())) {
				log.debug("Reloading stale collection '"+collectionId+"' config");
				cache.remove(elt.getKey());
				return super.getCollection(collectionId);
			} else {
				log.debug("Collection config for '"+collectionId+"' is up to date");
			}
			
			return c;
		}
	}
	
	@Override
	public List<String> getAllCollectionIds() {
		File configDirectory = new File(searchHome, DefaultValues.FOLDER_CONF);
		File[] collectionDirs = configDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				// Only directories that doesn't starts with a dot (.svn ...)
				return pathname.isDirectory() && !pathname.getName().startsWith(".");
			}
		});
			
		List<String> collectionIds = new ArrayList<String>();
		for (File collectionDir : collectionDirs) {
			collectionIds.add(collectionDir.getName());
		}
		Collections.sort(collectionIds);
		
		return collectionIds;			
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getGlobalConfigurationFile(GlobalConfiguration conf) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_globalConfigFile_.toString() + conf.toString();
		
		Element elt = cache.get(key);
		if (elt == null) {
			return super.getGlobalConfigurationFile(conf);
		} else {
			// File in cache
			File f = new File(searchHome + File.separator + DefaultValues.FOLDER_CONF, conf.getFileName());
			if(isFileStale(f, elt.getCreationTime())) {
				log.info("Configuration file '" + f.getAbsolutePath() + "' has changed and will be reloaded.");
				cache.remove(elt.getKey());
				return super.getGlobalConfigurationFile(conf);
			}
			return (Map<String, String>) elt.getObjectValue();
		}
	}
	
	@Override
	public Config getGlobalConfiguration() {
		if (globalConfiguration == null) {
			loadGlobalConfiguration();
		} else {
			Long now = System.currentTimeMillis();
			Long lastAccessTime = staleChecks.get(GlobalOnlyConfig.class.getName());
			staleChecks.put(GlobalOnlyConfig.class.getName(), now);
	
			// Don't check again exit if we've already checked recently
			if (lastAccessTime == null || now > (lastAccessTime+checkingInterval)) {
				if (globalConfiguration.isStale(searchHome)) {
					log.info("Global configuration data has changed and will be reloaded.");
					loadGlobalConfiguration();
				}
			}
		}
		
		return globalConfiguration;
	}


	@Override
	public String getExecutablePath(String exeName) {
		if (executablesMap == null) {
			loadExecutablesConfig();
		} else {
			Long now = System.currentTimeMillis();
			Long lastAccessTime = staleChecks.get(Files.EXECUTABLES_CONFIG_FILENAME);
			staleChecks.put(Files.EXECUTABLES_CONFIG_FILENAME, now);
	
			// Don't check again - exit if we've already checked recently
			if (lastAccessTime == null || now > (lastAccessTime+checkingInterval)) {
				try {
					long lastUpdated = Config.getLastUpdated(searchHome,Files.Types.EXECUTABLES,"");
					if (lastAccessTime == null || lastUpdated > lastAccessTime) {
						log.info("Executables configuration data has changed and will be reloaded.");
						loadExecutablesConfig();
					}
				} catch (FileNotFoundException e) {
					log.error("Checking to see if we should update the executables config",e);
				} catch (EnvironmentVariableException e) {
					log.error("Checking to see if we should update the executables config",e);
				}
			}
		}
		return executablesMap.get(exeName);
	}

	
	/**
	 * Checks if any of the source configuration files has changed since the element creation time.
	 * Doesn't detect actual changes but relies on file lastModified().
	 * @param c
	 * @param creationTime Date of the creation of the element in the cache
	 * @return true if any of the configuration has been updated, false otherwise.
	 */
	private boolean isAnyConfigFileStale(Collection c, long creationTime) {
		
		Long now = System.currentTimeMillis();
		Long lastAccessTime = staleChecks.get(c.getId());
		staleChecks.put(c.getId(), now);
		// Take an early exit if we've already check recently.
		if (lastAccessTime != null && now < (lastAccessTime+checkingInterval)) {
			return false;
		}
		
		if (c.getConfiguration().isStale(searchHome, creationTime) ) {
			log.debug("'" + c.getId() + "' collection configuration is stale.");
			return true;
		}
		
		File baseDataDir = new File(searchHome + File.separator + DefaultValues.FOLDER_DATA + File.separator + c.getId());
		
		// List of files to check for an update
		File[] files = new File[] {
				new File(c.getConfiguration().getConfigDirectory(), Files.FACETED_NAVIGATION_CONFIG_FILENAME),
				new File(baseDataDir + File.separator + DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX, Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory(), Files.META_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory(), Files.CGI_TRANSFORM_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory(), Files.QUICKLINKS_CONFIG_FILENAME),
		};

		// Hook scripts
		List<File> filesToCheck = new ArrayList<File>(Arrays.asList(files));
		for (Hook hook: Hook.values()) {
			filesToCheck.add(new File(c.getConfiguration().getConfigDirectory(), Files.HOOK_PREFIX + hook.toString() + Files.HOOK_SUFFIX));
		}
		
		for(File file: filesToCheck) {
			if (isFileStale(file, creationTime)) {
				log.info("Config file '" + file.getAbsolutePath() + "' has changed.");
				return true;
			}
		}
		
		// Check per-profile config files
		for(Profile p: c.getProfiles().values()) {
			files = new File[] {
					new File(c.getConfiguration().getConfigDirectory() + File.separator + p.getId(), Files.FACETED_NAVIGATION_CONFIG_FILENAME),
					new File(baseDataDir + File.separator + DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX + File.separator + p.getId(), Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME)
			};

			for(File file: files) {
				if (isFileStale(file, creationTime)) {
					log.info("Config file '" + file.getAbsolutePath() + "' has changed.");
					return true;
				}
			}
			
		}
		
		return false;
	}

	private boolean isFileStale(File f, long timestamp) {
		boolean existedPreviously = false;
		if (existingFiles.containsKey(f.getAbsolutePath())) {
			existedPreviously = existingFiles.get(f.getAbsolutePath());
		}
		
		existingFiles.put(f.getAbsolutePath(), f.canRead());
		if (existedPreviously != f.canRead()) {
			return true;
		} else {		
			return f.lastModified() > timestamp;
		}
	}

	
	/**
	 * Not cached, as it's rarely used and caching won't really help
	 */
	@Override
	public String[] getForms(String collectionId, String profileId) {
		return loadFormList(collectionId, profileId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getExtraSearchConfiguration(Collection collection, String extraSearchId) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_extraSearches_.toString() + collection.getId() + "_" + extraSearchId;
			
		Element elt = cache.get(key);
		if (elt == null) {
			return super.getExtraSearchConfiguration(collection, extraSearchId);
		} else {
			File config = new File(collection.getConfiguration().getConfigDirectory(),
					EXTRA_SEARCHES_PREFIX + "." + extraSearchId + CFG_SUFFIX);
			if (config.lastModified() > elt.getCreationTime()) {
				cache.remove(key);
				return super.getExtraSearchConfiguration(collection, extraSearchId);
			} else {
				return (Map<String, String>) elt.getObjectValue();
			}
		}
	}
	
}
