package com.funnelback.publicui.search.service.config;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.Synonym;
import com.funnelback.publicui.search.model.collection.paramtransform.ParamTransformRuleFactory;
import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.config.FacetedNavigationConfigParser.Facets;
import com.funnelback.publicui.xml.XmlParsingException;

/**
 * Convenience super class for local config repositories
 * (reading the configuration from the local disk).
 * 
 * Provides methods to load various config file, but the concrete
 * implementation of "getting" the collection is abstract, allowing
 * sub classes to cache the data for example.
 */
@lombok.extern.apachecommons.Log
public abstract class AbstractLocalConfigRepository implements ConfigRepository {
		
	/** A comment line in a config file starts with a hash */
	private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*#.*");
	
	/** Header line of the synonyms.cfg */
	private static final String SYNONYMS_HEADER = "PADRE Thesaurus Version: 2";
	
	@Autowired
	protected File searchHome;
	
	@Autowired
	private FacetedNavigationConfigParser fnConfigParser;
	
	@Override
	public abstract Collection getCollection(String collectionId);
	
	@Override
	public abstract List<String> getAllCollectionIds();
	
	protected Collection loadCollection(String collectionId) {
		log.info("Trying to load collection config for collection '" + collectionId + "'");
		try {
			Collection c = new Collection(collectionId, new NoOptionsConfig(searchHome, collectionId));
			loadFacetedNavigationConfig(c);
			c.setMetaComponents(loadMetaComponents(c));
			c.setParametersTransforms(loadParametersTransforms(c));
			c.setQuickLinksConfiguration(loadQuickLinksConfiguration(c));
			c.getProfiles().putAll(loadProfiles(c));
			c.getHookScriptsClasses().putAll(loadHookScriptsClasses(c));
			return c;
		} catch (FileNotFoundException e) {
			
			// TODO We must determine how to deal properly with config load error.
			// Currently there is now way to distinguish a "real" error (Such as no conf/
			// directory, or missing "global.cfg.default" file, from a "expeceted" error
			// such as "no collection with this id.
			
			// For now we'll return null in both cases, hiding any potential real error.
			log.error("Unable to load collection '" + collectionId + "'", e);
			return null;
		}
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
		File transformConfig = new File(c.getConfiguration().getConfigDirectory(), Files.FACETED_NAVIGATION_TRANSFORM_CONFIG_FILENAME);
		c.setFacetedNavigationConfConfig(readFacetedNavigationConfigs(fnConfig, transformConfig));
		
		// Read config in live/idx/
		try {
			fnConfig = new File(c.getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_IDX,
					Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
			c.setFacetedNavigationLiveConfig(readFacetedNavigationConfigs(fnConfig, transformConfig));
		} catch (FileNotFoundException fnfe) {
			log.error("Error while loading live faceted navigation configuration", fnfe);
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
		File transformConfig = new File(c.getConfiguration().getConfigDirectory() + File.separator +p.getId(), Files.FACETED_NAVIGATION_TRANSFORM_CONFIG_FILENAME);
		p.setFacetedNavConfConfig(readFacetedNavigationConfigs(fnConfig, transformConfig));
		
		// Read config in live/idx/<profile>/
		try {
			fnConfig = new File(c.getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_IDX
					+ File.separator + p.getId(),
					Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
			p.setFacetedNavLiveConfig(readFacetedNavigationConfigs(fnConfig, transformConfig));
		} catch (FileNotFoundException fnfe) {
			log.error("Error while loading live faceted navigation configuration", fnfe);
		}
	}
	
	/**
	 * Reads and parse a single faceted_navigation.cfg and associated faceted_navigation_transform.groovy
	 * @param fnConfig
	 * @return
	 */
	private FacetedNavigationConfig readFacetedNavigationConfigs(File fnConfig, File fnTransformConfig) {
		if (fnConfig.canRead()) {
			try {
				Facets f = fnConfigParser.parseFacetedNavigationConfiguration(FileUtils.readFileToString(fnConfig));
				return new FacetedNavigationConfig(f.qpOptions,f.facets, readGroovyScript(fnTransformConfig));
			} catch (IOException ioe) {
				log.error("Unable to read faceted navigation configuration from '" + fnConfig.getAbsolutePath() + "'", ioe);
				return null;
			} catch (XmlParsingException xpe) {
				log.error("Erro while parsing faceted navigation configuration from '" + fnConfig.getAbsolutePath() + "'", xpe);
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Loads meta.cfg
	 * @param c
	 * @return
	 */
	private String[] loadMetaComponents(Collection c) {
		return readConfig(c, new File(c.getConfiguration().getConfigDirectory(), Files.META_CONFIG_FILENAME));
	}
	
	/**
	 * Loads cgi_transform.cfg
	 * @param c
	 * @return
	 */
	private List<TransformRule> loadParametersTransforms(Collection c) {
		String[] rules = readConfig(c, new File(c.getConfiguration().getConfigDirectory(), Files.CGI_TRANSFORM_CONFIG_FILENAME));
		return ParamTransformRuleFactory.buildRules(rules);
	}
	
	/**
	 * Loads query_transform.groovy
	 * @param c
	 * @return
	 */
	private Map<Collection.Hook, Class<Script>> loadHookScriptsClasses(Collection c) {
		Map<Hook, Class<Script>> out = new HashMap<Hook, Class<Script>>();
		for (Hook hook: Hook.values()) {
			File hookScriptFile = new File(c.getConfiguration().getConfigDirectory(), Files.HOOK_PREFIX + hook.toString() + Files.HOOK_SUFFIX);
			Class<Script> hookScriptClass = readGroovyScript(hookScriptFile);
			if (hookScriptClass != null) {
				log.debug("Loaded hook script '" + hookScriptFile.getAbsolutePath() + "'");
				out.put(hook, hookScriptClass);
			}
		}
		return out;
	}
	
	/**
	 * Tries to parse a Groovy script
	 * @param scriptFile Path to the file containing the script
	 * @return a {@link Script} if successfully parsed, false otherwise
	 */
	@SuppressWarnings("unchecked")
	private Class<Script> readGroovyScript(File scriptFile) {
		if (scriptFile.canRead()) {
			try {
				return (Class<Script>) new GroovyClassLoader().parseClass(scriptFile);
			} catch (Exception e) {
				log.error("Unable to parse Groovy script '" + scriptFile + "'", e);
			}
		}
		return null;
	}
	
	/**
	 * Reads a config file and return its content.
	 * @param c Target collection
	 * @param configFile Config file to read
	 * @return Content of the file, or a zero-sized array in case of file not found or error.
	 */
	private String[] readConfig(Collection c, File configFile) {
		if (configFile.canRead()) {
			try {
				List<String> lines = FileUtils.readLines(configFile);				
				CollectionUtils.filter(lines, new RemoveCommentsPredicate());
				return lines.toArray(new String[0]);
			} catch (IOException ioe) {
				log.error("Unable to read configuration from '" + configFile.getAbsolutePath() + "'", ioe);
				return new String[0];
			}
		} else {
			return new String[0];
		}
	}
	
	/**
	 * Loads quicklinks.cfg
	 * @param c
	 * @return
	 */
	private Map<String, String> loadQuickLinksConfiguration(Collection c) {
		File qlConfig = new File(c.getConfiguration().getConfigDirectory(), Files.QUICKLINKS_CONFIG_FILENAME);
		try {
			return Config.readConfig(qlConfig.getAbsolutePath(), searchHome.getAbsolutePath(), c.getId());
		} catch (FileNotFoundException fnfe) {
			return new HashMap<String, String>(0);
		}
	}
	
	/**
	 * Loads synonyms.cfg
	 * @param c
	 * @return
	 * @deprecated Synonyms will be done by PADRE, see FUN-3368.
	 */
	@Deprecated
	private Synonym[] loadSynonyms(Collection c) {

		File[] synonymsConfigs = new File[] {
				new File(c.getConfiguration().getConfigDirectory(), Files.SYNONYMS_CONFIG_FILENAME),
				new File(c.getConfiguration().getConfigDirectory() + File.separator + DefaultValues.DEFAULT_PROFILE, Files.SYNONYMS_CONFIG_FILENAME)
		};
		
		for (File config: synonymsConfigs) {
			if (config.canRead()) {
				try {
					List<String> lines = FileUtils.readLines(config);
					if (SYNONYMS_HEADER.equals(lines.get(0))) {
						ArrayList<Synonym> synonyms = new ArrayList<Synonym>();
						for (int i=1; i<lines.size(); i++) {
							try {
								synonyms.add(Synonym.fromConfigLine(lines.get(i)));
							} catch (ParseException pe) {
								log.warn("Error while parsing synonym line '" + lines.get(i) + "'", pe);
							}
						}
						return synonyms.toArray(new Synonym[0]);
					} else {
						log.warn("Invalid Synonyms configuration. Unkown header '" + lines.get(0) + "'");
					}
				} catch (IOException ioe) {
					log.error("Unable to read synonyms configuration from '" + config.getAbsolutePath() + "'", ioe);
				}
			}
		}
		
		return new Synonym[0];		
	}
	
	/**
	 * Loads all the profile for a collection
	 * @param c
	 * @return
	 */
	private Map<String, Profile> loadProfiles(Collection c) {
		HashMap<String, Profile> out = new HashMap<String, Profile>();
		
		File[] profileDirs = c.getConfiguration().getConfigDirectory().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				// Only directories that doesn't starts with a dot (.svn ...)
				return pathname.isDirectory() && !pathname.getName().startsWith(".");
			}
		});
		
		for (File profileDir: profileDirs) {
			String id = profileDir.getName();
			Profile p = new Profile(id);
			loadFacetedNavigationConfig(c, p);
			out.put(p.getId(), p);
			log.debug("Loaded profile from '" + profileDir.getAbsolutePath() + "' for collection '" + c.getId() + "'");
		}
		
		return out;		
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
	 * Remove comments from config files
	 */
	private static class RemoveCommentsPredicate implements Predicate {
		@Override
		public boolean evaluate(Object o) {
			String line = (String) o;
			return ! COMMENT_PATTERN.matcher(line).matches();
		}
	};
	
}
