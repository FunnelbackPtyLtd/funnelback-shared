package com.funnelback.publicui.search.service.config;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
	protected enum CacheKeys {
		_CACHE_allCollectionIds,
		_CACHE_globalConfig_,
		_CACHE_lastUpdated_,
		_CACHE_forms_;
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

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getGlobalConfiguration(GlobalConfiguration conf) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_globalConfig_.toString() + conf.toString();
		
		Element elt = cache.get(key);
		if (elt == null) {
			Map<String, String> configData = loadGlobalConfiguration(conf);
			if (configData != null) {
				cache.put(new Element(key, configData));
			}
			return configData;
		} else {
			return (Map<String, String>) elt.getObjectValue();
		}
	}
	
	@Override
	public Date getLastUpdated(String collectionId) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_lastUpdated_.toString() + collectionId;
		
		Element elt = cache.get(key);
		if (elt == null) {
			return loadLastUpdated(collectionId);
		} else {
			return (Date) elt.getObjectValue();
		}
	}
	
	@Override
	public String[] getForms(String collectionId, String profileId) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_forms_.toString() + collectionId;
		
		Element elt = cache.get(key);
		if (elt == null) {
			return loadFormList(collectionId, profileId);
		} else {
			return (String[]) elt.getObjectValue();
		}
	}

}
