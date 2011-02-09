package com.funnelback.publicui.search.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * {@link AbstractLocalConfigRepository} implementation that put
 * collection config data in a non auto-refreshing cache.
 */
public class CachedLocalConfigRepository extends AbstractLocalConfigRepository {

	/** Identifier of the EHCache used */
	protected static final String CACHE = "localConfigRepository";
	private enum CacheKeys {
		_CACHE_allCollectionIds;
	}

	@Autowired
	protected CacheManager appCacheManager;
	

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
