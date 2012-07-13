package com.funnelback.publicui.search.service.resource;

import java.io.File;
import java.io.IOException;

import lombok.Setter;
import lombok.extern.log4j.Log4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default resource manager that will cache parsed file and reloads them if the
 * corresponding file changes.
 */
@Component
@Log4j
public class AutoRefreshResourceManager implements ResourceManager {

	private static final String CACHE = "localConfigFilesRepository";

	@Autowired
	@Setter
	protected CacheManager appCacheManager;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T load(File f, ResourceParser<T> p) throws IOException {

		Cache cache = appCacheManager.getCache(CACHE);
		T object = null;

		// Is the file in the cache ?
		String key = f.getAbsolutePath();
		Element elt = cache.get(key);
		if (elt == null) {
			log.debug("Java object for file '" + key + "' not found in cache");
			object = p.parse(f);
			cache.put(new Element(key, object));
		} else {
			log.debug("Java object for file '" + key + "' found in cache");
			if (f.lastModified() > elt.getLatestOfCreationAndUpdateTime()) {
				log.debug("File '"+key+"' has changed, reloading it");
				object = p.parse(f);
				cache.put(new Element(key, object));
			} else {
				object = (T) elt.getObjectValue();
			}
		}
		
		return object;
	}

}
