package com.funnelback.publicui.search.service.config;

import groovy.lang.Script;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import lombok.Setter;
import lombok.extern.log4j.Log4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.ConfigReader;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.resource.ResourceManager;
import com.funnelback.publicui.search.service.resource.impl.AbstractSingleFileResource;
import com.funnelback.publicui.search.service.resource.impl.ConfigMapResource;
import com.funnelback.publicui.search.service.resource.impl.ConfigResource;
import com.funnelback.publicui.search.service.resource.impl.FacetedNavigationConfigResource;
import com.funnelback.publicui.search.service.resource.impl.GlobalConfigResource;
import com.funnelback.publicui.search.service.resource.impl.GroovyScriptResource;
import com.funnelback.publicui.search.service.resource.impl.ParameterTransformResource;
import com.funnelback.publicui.search.service.resource.impl.PropertiesResource;
import com.funnelback.publicui.search.service.resource.impl.SimpleFileResource;
import com.funnelback.publicui.search.service.resource.impl.UniqueLinesResource;
import com.funnelback.publicui.utils.MapUtils;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser;

/**
 * <p>Default {@link ConfigRepository} implementation.</p>
 * 
 * <p>Relies on a {@link ResourceManager} to cache access to
 * the underlying files</p>
 */
@Repository("configRepository")
@Log4j
public class DefaultConfigRepository implements ConfigRepository {
			
	public static final String FTL_SUFFIX = ".ftl";
	protected static final String CFG_SUFFIX = ".cfg";
	
	private static final Pattern FORM_BACKUP_PATTERN = Pattern.compile("-\\d{12}"+FTL_SUFFIX);
	
	private static final String EXTRA_SEARCHES_PREFIX = "extra_search";
	
	private static final String CACHE = "localConfigRepository";
	
	/**
	 * <p>Cache TTL. No need to set it to less than 1s because
	 * ext3 time resolution is 1s anyway.</p>
	 */
	@Value("#{appProperties['config.repository.cache.ttl']?:1}")
	@Setter
	private int cacheTtlSeconds = 1;
	
	@Autowired
	@Setter
	protected CacheManager appCacheManager;
	
	@Autowired
	@Setter
	protected ResourceManager resourceManager;
	
	@Autowired
	@Setter
	protected File searchHome;
	
	@Autowired
	@Setter
	private FacetedNavigationConfigParser fnConfigParser;
	
	/**
	 * <p>This implementation will cache collection objects for a short period
	 * so that multiple call to this function for a single request will
	 * not cause the Collection object to be "rebuilt" from all configuration
	 * data.</p>
	 * 
	 * <p>Note that the configuration data is cached anyway by the {@link ResourceManager}
	 * abstraction, however getting all the configuration for the cache and assembling it
	 * is still a bit expensive.</p>
	 */
	@Override
	public Collection getCollection(String collectionId) {
		// Cache will never be null
		Cache cache = appCacheManager.getCache(CACHE);
		Element elt = cache.get(collectionId);
		
		if (elt == null) {
			try {
				Collection c = loadCollection(collectionId);
				Element e = new Element(collectionId, c);
				// Expire element after 1 second so that it's short-lived
				// only for the current request.
				e.setTimeToLive(cacheTtlSeconds);
				cache.put(e);
				return c;
			} catch (IOException ioe) {
				log.error("Unable to load collection '" + collectionId + "'", ioe);
				return null;
			}
		} else {
			return (Collection) elt.getObjectValue();
		}
	}
	
	/**
	 * Loads a collection
	 * @param collectionId Collection to load
	 * @return The collection object, or null if not found
	 * @throws IOException
	 */
	private Collection loadCollection(String collectionId) throws IOException {
		log.debug("Loading collection configuration for '"+collectionId+"'");
		File configFolder = new File(searchHome+File.separator+DefaultValues.FOLDER_CONF, collectionId);
		
		Config config = resourceManager.load(new ConfigResource(searchHome, collectionId));
		if (config == null) {
			return null;
		}
		
		Collection c = new Collection(collectionId, config);
		
		loadFacetedNavigationConfig(c);
		
		if (Type.meta.equals(c.getType())) {
			c.setMetaComponents(
					resourceManager.load(
							new SimpleFileResource(new File(configFolder, Files.META_CONFIG_FILENAME)),
							new String[0]));
		} else {
			c.setMetaComponents(new String[0]);
		}

		c.getTextMinerBlacklist().addAll(
				resourceManager.load(
						new UniqueLinesResource(new File(configFolder, Files.TEXT_MINER_BLACKLIST)),
						new HashSet<String>(0)));

		c.getParametersTransforms().addAll(
				resourceManager.load(
						new ParameterTransformResource(new File(configFolder, Files.CGI_TRANSFORM_CONFIG_FILENAME)),
						new ArrayList<TransformRule>(0)));
		c.setQuickLinksConfiguration(resourceManager.load(new ConfigMapResource(
				collectionId,
				searchHome,
				new File(configFolder, Files.QUICKLINKS_CONFIG_FILENAME)), new HashMap<String, String>(0)));

		c.getProfiles().putAll(loadProfiles(c));
		
		for (Hook hook: Hook.values()) {
			File hookScriptFile = new File(configFolder, Files.HOOK_PREFIX + hook.toString() + Files.HOOK_SUFFIX);
			if (hookScriptFile.exists()) {
				try {
					Class<Script> hookScript = resourceManager.load(new GroovyScriptResource(hookScriptFile));
					c.getHookScriptsClasses().put(hook, hookScript);
				} catch (CompilationFailedException cfe) {
					log.error("Compilation of hook script '"+hookScriptFile+"' failed", cfe);
				}
			}
		}
		
		return c;
	}
	
	/**
	 * Loads the profiles for a given collection
	 * @param c
	 * @return
	 */
	private Map<String, Profile> loadProfiles(Collection c) {
		HashMap<String, Profile> out = new HashMap<String, Profile>();
		
		for (File profileDir: getProfileDirs(c.getConfiguration().getConfigDirectory())) {
			Profile p = new Profile(profileDir.getName());
			loadFacetedNavigationConfig(c, p);
			File padreOptsFile = new File(profileDir + File.separator + Files.PADRE_OPTS);
			try {
				p.setPadreOpts(resourceManager.load(new AbstractSingleFileResource<String>(padreOptsFile) {
						@Override
						public String parse() throws IOException {
							return FileUtils.readFileToString(file);
						}
					}));
			} catch (IOException e) {
				log.error("Could not read padre opts file from '"+padreOptsFile+"'",e);
			}

			out.put(p.getId(), p);
			log.debug("Loaded profile from '" + profileDir.getAbsolutePath() + "' for collection '" + c.getId() + "'");
		}
		
		return out;		
	}

	/**
	 * Returns the list of profiles for a given collection, by looking
	 * in the collection configuration folder
	 * @param configDir
	 * @return
	 */
	private File[] getProfileDirs(File configDir) {
		return configDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				// Only directories that doesn't starts with a dot (.svn ...)
				return pathname.isDirectory() && !pathname.getName().startsWith(".");
			}
		});
	}

	/**
	 * Loads faceted_navigation.cfg, for a collection, from multiple locations:
	 * - Global in conf/faceted_navigation.cfg
	 * - Live in live/idx/faceted_navigation.xml
	 * @param c
	 * @return
	 */
	private void loadFacetedNavigationConfig(Collection c) {
		// Read global config in conf/
		File fnConfig = new File (c.getConfiguration().getConfigDirectory(), Files.FACETED_NAVIGATION_CONFIG_FILENAME);
		try {
			c.setFacetedNavigationConfConfig(resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser)));
			
			// Read config in live/idx/
			fnConfig = new File(c.getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_IDX,
					Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
			c.setFacetedNavigationLiveConfig(resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser)));
		} catch (IOException ioe) {
			log.error("Unable to read faceted navigation configuration from '" + fnConfig.getAbsolutePath() + "'", ioe);
		}
	}
	
	/**
	 * Loads faceted_navigation.cfg, for a profile, from multiple locations:
	 * - Global in conf/<profile>/faceted_navigation.cfg
	 * - Live in live/idx/<profile>/faceted_navigation.xml
	 * @param c
	 * @param p
	 */
	private void loadFacetedNavigationConfig(Collection c, Profile p) {
		// Read global config in conf/<profile>/
		File fnConfig = new File (c.getConfiguration().getConfigDirectory() + File.separator + p.getId(), Files.FACETED_NAVIGATION_CONFIG_FILENAME);
		try {
			p.setFacetedNavConfConfig(resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser)));
			
			// Read config in live/idx/<profile>/
			fnConfig = new File(c.getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_IDX
					+ File.separator + p.getId(),
					Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
			p.setFacetedNavLiveConfig(resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser)));
		} catch (IOException ioe) {
			log.error("Unable to read faceted navigation configuration from '" + fnConfig.getAbsolutePath() + "'", ioe);
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
	
	@Override
	public Map<String, String> getGlobalConfigurationFile(GlobalConfiguration conf) {
		try {
			return resourceManager.load(
					new ConfigMapResource(searchHome,
							new File(searchHome+File.separator+DefaultValues.FOLDER_CONF, conf.getFileName())));
		} catch (IOException ioe) {
			log.fatal("Could not load global configuration file '"+conf.getFileName()+"'", ioe);
			throw new RuntimeException(ioe);
		}
	}	
	
	@Override
	public List<Collection> getAllCollections() {
		List<Collection> collections = new ArrayList<Collection>();
		for (String collectionId: getAllCollectionIds()) {
			Collection collection = getCollection(collectionId);
			if (collection != null) {
				collections.add(collection);
			}			
		}
		return collections;
	}

	/**
	 * Loads the executables config file
	 */
	
	public String getExecutablePath(String exeName) {
		File executablesCfg = new File(new File(searchHome, DefaultValues.FOLDER_CONF), Files.EXECUTABLES_CONFIG_FILENAME);
		try {
			Map<String, String> m = resourceManager.load(
				new AbstractSingleFileResource<Map<String, String>>(executablesCfg) {

					@Override
					public Map<String, String> parse() throws IOException {
						Map<String, String> out = new HashMap<String, String>();
						Map<String, String> uncleanMap = ConfigReader.readConfig(file, searchHome);
						
						// replace quotes at the ends of the executable names (if any)
						for(Entry<String,String> entry : uncleanMap.entrySet()) {
							String value = entry.getValue();
							if(value != null) {
								if((value.charAt(0) == '"' && value.charAt(value.length() -1) == '"' )|| (value.charAt(0) == '\'' && value.charAt(value.length() -1) == '\'' )) {
									value = value.substring(1, value.length() -1);	
								}
							}
							out.put(entry.getKey(), value);
						}
						
						return out;
					}

					
				});
			return m.get(exeName);
		} catch (Exception ioe) {
			log.error("Could not load executables config from '"+executablesCfg+"'", ioe);
			throw new RuntimeException(ioe);
		}
	}

	public String[] getForms(String collectionId, String profileId) {
		if (collectionId == null || profileId == null) {
			throw new IllegalArgumentException("collectionId and profileId cannot be null");
		}
		Collection c = getCollection(collectionId);
		if (c == null) {
			throw new IllegalArgumentException("Invalid collection '" + collectionId + "'");
		}
		
		// Find form files, excluding backups (simple-20110101093000.ftl)
		File profileDir = new File(c.getConfiguration().getConfigDirectory() + File.separator + profileId);
		if (!profileDir.exists()) {
			return new String[0];
		}
		
		File[] formFiles = profileDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile()
					&& pathname.getName().endsWith(FTL_SUFFIX)
					&& ! FORM_BACKUP_PATTERN.matcher(pathname.getName()).matches();
			}
		});
		
		// Remove .ftl suffix
		String[] out = new String[formFiles.length];
		for (int i=0; i<formFiles.length; i++) {
			out[i] = formFiles[i].getName().replaceAll(FTL_SUFFIX+"$", "");
		}
		
		return out;
		
	}
	
	public Map<String, String> getExtraSearchConfiguration(Collection collection, String extraSearchId) {
		File config = new File(collection.getConfiguration().getConfigDirectory(),
				EXTRA_SEARCHES_PREFIX + "." + extraSearchId + CFG_SUFFIX);
		
		try {
			Properties p = resourceManager.load(new PropertiesResource(config));
			if (p != null) {
				return MapUtils.fromProperties(p);
			} else {
				log.warn("Extra searches configuration file '"+config+"' doesn't exist");
			}
		} catch (IOException ioe) {
			log.error("Could not load extra seach config file from '"+config+"'", ioe);
		}
		
		return null;
	}

	@Override
	public Config getGlobalConfiguration() {
		try {
			return resourceManager.load(new GlobalConfigResource(searchHome));
		} catch (IOException ioe) {
			log.fatal("Could not load global configuration", ioe);
			throw new RuntimeException(ioe);
		}
	}
	
	@Override
	public Map<String, String> getTranslations(String collectionId,	String profileId, Locale locale) {
		Map<String, String> out = new HashMap<String, String>();
		Map<String, String> emptyMap = new HashMap<String, String>(0);
		
		// Possible files: Global config, language specific,
		// language + country specific
		List<String> filenames = new ArrayList<String>();
		filenames.add(Files.UI_I18N);
		if (locale.getLanguage() != null) {
			filenames.add(Files.UI_I18N_PREFIX + "." + locale.getLanguage() + Files.UI_I18N_SUFFIX);
			if (locale.getCountry() != null) {
				filenames.add(Files.UI_I18N_PREFIX + "." + locale.getLanguage() + "_" + locale.getCountry() + Files.UI_I18N_SUFFIX);
			}
		}

		// Folders to look into
		List<File> folders = new ArrayList<File>();
		folders.add(new File(searchHome, DefaultValues.FOLDER_CONF));
		if (profileId != null && ! "".equals(profileId)) {
				folders.add(new File(searchHome
						+ File.separator + DefaultValues.FOLDER_CONF
						+ File.separator + collectionId,
						profileId));
		};

		// Lookup general files first, then more specific ones
		for (String filename: filenames) {
			for (File folder: folders) {
				File f = new File(folder, filename);
				try {
					out.putAll(resourceManager.load(new ConfigMapResource(searchHome, f), emptyMap));
				} catch (IOException ioe) {
					log.error("Unable to load translation bundle from '"+f.getAbsolutePath()+"'", ioe);
				}

			}
		}
		
		return out;
	}
	
	@Override
	public File getXslTemplate(String collectionId, String profileId) {
		File template = new File(searchHome
				+ File.separator + DefaultValues.FOLDER_CONF
				+ File.separator + collectionId
				+ File.separator + profileId,
				Files.XSL_TEMPLATE);
		
		if (template.exists()) {
			return template;
		} else {
			return null;
		}
	}
	
}
