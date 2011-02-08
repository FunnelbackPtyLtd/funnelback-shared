package com.funnelback.publicui.search.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Synonym;
import com.funnelback.publicui.search.service.ConfigRepository;

@Repository("configRepository")
@lombok.extern.apachecommons.Log
public class LocalConfigRepository implements ConfigRepository {
	
	private static final String CACHE = "localConfigRepository";
	private enum CacheKeys {
		_CACHE_allCollectionIds;
	}
	
	private static final Pattern FACETED_NAVIGATION_QPOPTIONS_PATTERN = Pattern.compile("qpoptions=\"([\\w -]*)\"");
	
	/** A comment line in a config file starts with a hash */
	private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*#.*");
	
	/** Header line of the synonyms.cfg */
	private static final String SYNONYMS_HEADER = "PADRE Thesaurus Version: 2";
	
	@Autowired
	private CacheManager appCacheManager;
	
	@Autowired
	private File searchHome;
	
	@Override
	public Collection getCollection(String collectionId) {
		// Cache will never be null
		Cache cache = appCacheManager.getCache(CACHE);

		Element elt = cache.get(collectionId);
		if (elt == null) {
			Collection collection = loadCollection(collectionId);
			if (collection != null) {
				cache.put(new Element(collectionId, collection));
			}
			return collection;
		} else {
			return (Collection) elt.getObjectValue();
		}
	}
	
	protected Collection loadCollection(String collectionId) {
		log.info("Trying to load collection config for collection '" + collectionId + "'");
		try {
			Collection c = new Collection(collectionId, new NoOptionsConfig(searchHome, collectionId));
			c.setFacetedNavigationConfig(loadFacetedNavigationConfig(c));
			c.setMetaComponents(loadMetaComponents(c));
			c.setParametersTransforms(loadParametersTransforms(c));
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
	 * Loads faceted_navigation.cfg
	 * @param c
	 * @return
	 */
	private static FacetedNavigationConfig loadFacetedNavigationConfig(Collection c) {
		File fnConfig = new File(c.getConfiguration().getConfigDirectory(), Files.FACETED_NAVIGATION_CONFIG_FILENAME);
		if (fnConfig.canRead()) {
			try {
				// TODO Implement proper XML parsing. For now we're only interested in the
				// 'qpoptions' attribute
				String config = IOUtils.toString(new FileInputStream(fnConfig));
				Matcher m = FACETED_NAVIGATION_QPOPTIONS_PATTERN.matcher(config);
				if (m.find()) {
					return new FacetedNavigationConfig(m.group(1).trim());
				}
			
				// We found a faceted navigation config, but no query processor option
				// That shouldn't occur
				log.warn("Faceted navigation configuration for collection '" + c.getId() + "' doesn't contain 'qpoptions':\n" + config);
				return null;
			} catch (IOException ioe) {
				log.error("Unable to read faceted navigation configuration from '" + fnConfig.getAbsolutePath() + "'", ioe);
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
	private static String[] loadMetaComponents(Collection c) {
		return readConfig(c, new File(c.getConfiguration().getConfigDirectory(), Files.META_CONFIG_FILENAME));
	}
	
	/**
	 * Loads cgi_transform.cfg
	 * @param c
	 * @return
	 */
	private static String[] loadParametersTransforms(Collection c) {
		return readConfig(c, new File(c.getConfiguration().getConfigDirectory(), Files.CGI_TRANSFORM_CONFIG_FILENAME));
	}
	
	/**
	 * Reads a config file and return its content.
	 * @param c Target collection
	 * @param configFile Config file to read
	 * @return Content of the file, or a zero-sized array in case of file not found or error.
	 */
	private static String[] readConfig(Collection c, File configFile) {
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
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllCollectionIds() {
		Cache cache = appCacheManager.getCache(CACHE);
		
		Element elt = cache.get(CacheKeys._CACHE_allCollectionIds);
		if (elt == null) {
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
			cache.put(new Element(CacheKeys._CACHE_allCollectionIds, collectionIds));
			
			return collectionIds;			
		} else {
			return (List<String>) elt.getObjectValue();
		}
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
