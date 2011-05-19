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

import com.funnelback.common.config.Config;
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
		_CACHE_globalConfigFile_,
		_CACHE_globalConfiguration_,
		_CACHE_lastUpdated_,
		_CACHE_forms_,
		_CACHE_extraSearches_;
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
	public Map<String, String> getGlobalConfigurationFile(GlobalConfiguration conf) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_globalConfigFile_.toString() + conf.toString();
		
		Element elt = cache.get(key);
		if (elt == null) {
			Map<String, String> configData = loadGlobalConfigurationFile(conf);
			if (configData != null) {
				cache.put(new Element(key, configData));
			}
			return configData;
		} else {
			return (Map<String, String>) elt.getObjectValue();
		}
	}
	
	@Override
	public Config getGlobalConfiguration() {
		if (globalConfiguration == null) {
			loadGlobalConfiguration();
		}
		return globalConfiguration;
	}
	
	@Override
	public Date getLastUpdated(String collectionId) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_lastUpdated_.toString() + collectionId;
		
		Element elt = cache.get(key);
		if (elt == null) {
			Date lastUpdated =  loadLastUpdated(collectionId);
			if (lastUpdated != null) {
				cache.put(new Element(key, lastUpdated));
			}
			return lastUpdated;
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
			String[] forms = loadFormList(collectionId, profileId);
			if ( forms != null) {
				cache.put(new Element(key, forms));
			}
			return forms;
		} else {
			return (String[]) elt.getObjectValue();
		}
	}
	
	@Override
	public Map<String, String> getExtraSearchConfiguration(Collection collection, String extraSearchId) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_extraSearches_.toString() + collection.getId() + "_" + extraSearchId;
			
		Element elt = cache.get(key);
		if (elt == null) {
			Map<String, String> config = loadExtraSearchConfiguration(collection, extraSearchId);
			if ( config != null) {
				cache.put(new Element(key, config));
			}
			return config;
		} else {
			return (Map<String, String>) elt.getObjectValue();
		}
	}

}
