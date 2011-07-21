package com.funnelback.publicui.search.service.index;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class CachedLocalIndexRepository extends AbstractLocalIndexRepository {


	/** Identifier of the EHCache used */
	protected static final String CACHE = "localIndexRepository";
	protected enum CacheKeys {
		_CACHE_lastUpdated_
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

}
