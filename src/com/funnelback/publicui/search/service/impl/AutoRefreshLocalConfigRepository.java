package com.funnelback.publicui.search.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Repository;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * Implementation of {@link AbstractLocalConfigRepository} that caches config
 * data and will auto-refresh its cache if any of the underlying files has changed.
 */
@Repository("configRepository")
@Log
public class AutoRefreshLocalConfigRepository extends CachedLocalConfigRepository {

	@Override
	public Collection getCollection(String collectionId) {
		Cache cache = appCacheManager.getCache(CACHE);
		Element elt = cache.get(collectionId);
		
		if (elt == null) {
			// Collection never loaded
			return super.getCollection(collectionId); 
		} else {
			// Collection in cache
			Collection c = (Collection) elt.getObjectValue();
			
			if(isAnyConfigFileStale(c, elt.getCreationTime())) {
				cache.remove(elt.getKey());
				return super.getCollection(collectionId);
			}
			
			return c;
		}
	}
	
	@SuppressWarnings("unchecked")
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

	/**
	 * Checks if any of the source configuration files has changed since the specified timestamp.
	 * Doesn't detect actual changes but relies on file lastModified().
	 * @param c
	 * @param timestamp
	 * @return true if any of the configuration has been updated, false otherwise.
	 */
	private boolean isAnyConfigFileStale(Collection c, long timestamp) {
		if (c.getConfiguration().isStale(searchHome, timestamp) ) {
			log.debug("'" + c.getId() + "' collection configuration is stale.");
			return true;
		}
		
		File[] filesToCheck = new File[] {
				new File(c.getConfiguration().getConfigDirectory(), Files.FACETED_NAVIGATION_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory(), Files.META_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory(), Files.CGI_TRANSFORM_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory(), Files.QUICKLINKS_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory(), Files.SYNONYMS_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory() + File.separator + DefaultValues.DEFAULT_PROFILE, Files.SYNONYMS_CONFIG_FILENAME)
		};
		
		for(File file: filesToCheck) {
			if (file.lastModified() > timestamp) {
				log.debug("Config file '" + file.getAbsolutePath() + "' has changed.");
				return true;
			}
		}
		
		return false;
		
	}
	

}
