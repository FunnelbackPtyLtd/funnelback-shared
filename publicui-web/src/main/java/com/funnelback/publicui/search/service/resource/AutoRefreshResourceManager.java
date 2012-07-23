package com.funnelback.publicui.search.service.resource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.Setter;
import lombok.extern.log4j.Log4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Default resource manager that will cache parsed file and reloads them if the
 * corresponding file has changed or has been deleted / created.
 */
@Component
@Log4j
public class AutoRefreshResourceManager implements ResourceManager {

	private static final String CACHE = "localConfigFilesRepository";
	
	/**
	 * <p>How often should we check files for freshness ?</p>
	 * 
	 * <p>That's especially useful in the context of a single HTTP request
	 * when we want to check only once per request (no need checking if a
	 * file has changed during the request processing.</p>
	 */
	@Value("#{appProperties['config.repository.autorefresh.interval']?:250}")
	@Setter
	private int checkingInterval = 0;
	
	/**
	 * Keep track of recent stale checks to avoid checking all
	 * the resources too often
	 */
	private Map<Object, Long> staleChecks = Collections.synchronizedMap(new HashMap<Object, Long>());

	@Autowired
	@Setter
	protected CacheManager appCacheManager;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T load(ParseableResource<T> p, T valueIfNonExistent) throws IOException {
		T object = valueIfNonExistent;
		
		Cache cache = appCacheManager.getCache(CACHE);
		

		// Is the resource in the cache ?
		Object key = p.getCacheKey();
		Element elt = cache.get(key);
		
		if (elt == null) {
			if (p.exists()) {
				log.debug("Java object for resource'" + key + "' not found in cache");
				object = p.parse();
				cache.put(new Element(key, object));
			} else {
				log.debug("Resource '"+key+"' doesn't exist, nothing to do.");
			}
		} else {
			log.debug("Java object for file '" + key + "' found in cache");
			if (!p.exists()) {
				log.debug("Resource'"+key+"' has been deleted. Removing cache entry");
				cache.remove(key);
			} else if (shouldCheck(key) && p.isStale(elt.getLatestOfCreationAndUpdateTime())) {
				log.debug("File '"+key+"' has changed, reloading it");
				object = p.parse();
				cache.put(new Element(key, object));
			} else {
				object = (T) elt.getObjectValue();
			}
		}
		
		return object;
	}
	
	@Override
	public <T> T load(ParseableResource<T> resource) throws IOException {
		return load(resource, null);
	}
	
	/**
	 * Resources are not checked for staleness everytime, but
	 * only once every N milliseconds to avoid checking too often.
	 * @param key
	 * @return
	 */
	private boolean shouldCheck(Object key) {
		// Only check every 'checkingInterval' ms
		Long lastTimeChecked = staleChecks.get(key);
		if (lastTimeChecked == null || lastTimeChecked+checkingInterval < System.currentTimeMillis()) {
			staleChecks.put(key, System.currentTimeMillis());
			return true;
		} else {	
			return false;
		}
	}

}
