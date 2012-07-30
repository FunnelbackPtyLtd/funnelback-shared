package com.funnelback.publicui.search.service.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j
@Component
public class DefaultImageFetcher implements ImageFetcher {
	
	protected static final String CACHE = DefaultImageFetcher.class.getSimpleName() + "Repository";

	@Autowired
	protected CacheManager appCacheManager;

	@Override
	public byte[] fetch(String url) throws IOException {
		
		Cache cache = appCacheManager.getCache(CACHE);

		if (! cache.isKeyInCache(url)) {
			log.trace("Fetching " + url + " to cache");

			URL imageUrl = new URL(url);
			@Cleanup InputStream imageStream = imageUrl.openStream();
			cache.put(new Element(url, IOUtils.toByteArray(imageStream)));
		}

		return (byte[])cache.get(url).getValue();
	}



}
