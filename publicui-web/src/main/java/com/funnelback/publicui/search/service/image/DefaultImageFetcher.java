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

import com.funnelback.publicui.search.service.ConfigRepository;

@Log4j
@Component
public class DefaultImageFetcher implements ImageFetcher {
    
    protected static final String CACHE = DefaultImageFetcher.class.getSimpleName() + "Repository";
    protected static final String PATTERN_KEY = "default_image_fetcher.permitted_url_pattern";

    @Autowired
    protected CacheManager appCacheManager;
    
    @Autowired
    private ConfigRepository configRepository;

    @Override
    public byte[] fetch(String url) throws IOException {
        String permittedUrlPattern = configRepository.getGlobalConfiguration().value(PATTERN_KEY);
        if (permittedUrlPattern != null && !permittedUrlPattern.isEmpty()) {
            // Compare the URL to the pattern - Fail if it doesn't match
            if (! url.matches(permittedUrlPattern)) {
                throw new RuntimeException("URL is not permitted according to " + PATTERN_KEY);
            }
        }
        
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
