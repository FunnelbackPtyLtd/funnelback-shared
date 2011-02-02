package com.funnelback.publicui.search.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.FacetedNavigationConfig;
import com.funnelback.publicui.search.service.ConfigRepository;

@Repository("configRepository")
@lombok.extern.apachecommons.Log
public class LocalConfigRepository implements ConfigRepository {
	
	private static final String CACHE = "localConfigRepository";
	private enum CacheKeys {
		_CACHE_allCollectionIds;
	}
	
	private static final Pattern FACETED_NAVIGATION_QPOPTIONS_PATTERN = Pattern.compile("qpoptions=\"([\\w -]*)\"");
	
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
	
	private FacetedNavigationConfig loadFacetedNavigationConfig(Collection c) {
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
				log.error("Unable to read faceted navigation configuration from " + fnConfig.getAbsolutePath(), ioe);
				return null;
			}
		} else {
			return null;
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

}
