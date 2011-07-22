package com.funnelback.publicui.search.service.index;

import java.util.Date;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;

public class CachedLocalIndexRepository extends AbstractLocalIndexRepository {


	/** Identifier of the EHCache used */
	protected static final String CACHE = "localIndexRepository";
	protected enum CacheKeys {
		_CACHE_lastUpdated_,
		_CACHE_bldinfo_
	}

	@Autowired
	protected CacheManager appCacheManager;
	
	
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
	
	@SuppressWarnings("unchecked")
	@Override
	public String getBuildInfoValue(final String collectionId, final String key) {
		Cache cache = appCacheManager.getCache(CACHE);
		String cacheKey = CacheKeys._CACHE_bldinfo_.toString() + collectionId;
		
		Element elt = cache.get(cacheKey);
		if (elt == null) {
			Map<String, String> bldInfo = loadBuildInfo(collectionId);
			if (bldInfo != null) {
				elt = new Element(cacheKey, bldInfo);
				cache.put(elt);
			} else {
				return null;
			}
		}
		
		return ((Map<String, String>) elt.getObjectValue()).get(key);
		
		
	}

}
