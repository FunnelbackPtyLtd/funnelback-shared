package com.funnelback.publicui.search.service.index;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.funnelback.publicui.search.service.IndexRepository;

/**
 * Implementation of {@link IndexRepository} that caches data and
 * will auto-refresh its cache if any of the underlying files has changed.
 */
@Repository("indexRepository")
public class AutoRefreshLocalIndexRepository extends CachedLocalIndexRepository {

	@Value("#{appProperties['config.repository.autorefresh.interval']}")
	private int checkingInterval = 0;
	
	/**
	 * Keep track of recent stale checks to avoid checking all
	 * the files to often
	 */
	private Map<String, Long> staleChecks = new HashMap<String, Long>();
	
	/**
	 * Prefix used to store the last time the "index_time" file has been
	 * checked for a specific collection, in the staleChecks map.
	 */
	private static final String LAST_UPDATED_STALE_KEY_PREFIX = "_LAST_UPDATED_DATE_";
	
	/** Prefix used to store the last time the <code>index.bldinfo</code> file
	 * has been checked for a specific collection, in the {@link #staleChecks} map.
	 */
	private static final String BLDINFO_STALE_KEY_PREFIX = "_BLDINFO_";
	
	@Override
	public Date getLastUpdated(String collectionId) {
		Cache cache = appCacheManager.getCache(CACHE);
		String key = CacheKeys._CACHE_lastUpdated_.toString() + collectionId;
		
		Element elt = cache.get(key);
		if (elt == null) {
			return loadLastUpdated(collectionId);
		} else {
			Long now = System.currentTimeMillis();
			Long lastAccessTime = staleChecks.get(LAST_UPDATED_STALE_KEY_PREFIX + collectionId);
			staleChecks.put(LAST_UPDATED_STALE_KEY_PREFIX + collectionId, now);
			// Take an early exit if we've already check recently.
			if (lastAccessTime != null && now < (lastAccessTime+checkingInterval)) {
				return (Date) elt.getObjectValue();
			} else {
				cache.remove(elt.getKey());
				return super.getLastUpdated(collectionId);
			}		
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getBuildInfoValue(final String collectionId, final String key) {
		Cache cache = appCacheManager.getCache(CACHE);
		String cacheKey = CacheKeys._CACHE_bldinfo_.toString() + collectionId;
		
		Element elt = cache.get(cacheKey);
		if (elt == null) {
			return super.getBuildInfoValue(collectionId, key);
		} else {
			Long now = System.currentTimeMillis();
			Long lastAccessTime = staleChecks.get(BLDINFO_STALE_KEY_PREFIX + collectionId);
			staleChecks.put(BLDINFO_STALE_KEY_PREFIX+collectionId, now);
			if (lastAccessTime != null && now < (lastAccessTime+checkingInterval)) {
				return ((Map<String, String>) elt.getObjectValue()).get(key); 
			} else {
				cache.remove(elt.getKey());
				return super.getBuildInfoValue(collectionId, key);
			}
		}
	}

	
}
