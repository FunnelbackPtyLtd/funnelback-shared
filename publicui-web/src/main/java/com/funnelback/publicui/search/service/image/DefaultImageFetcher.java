package com.funnelback.publicui.search.service.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.Cleanup;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.service.ConfigRepository;
import com.google.common.io.ByteStreams;

@Log4j2
@Component
public class DefaultImageFetcher implements ImageFetcher {
    
    public static final String CACHE_NAME = DefaultImageFetcher.class.getSimpleName() + "Repository";
    public static final String PATTERN_KEY = "default_image_fetcher.permitted_url_pattern";
    public static final String SIZE_LIMIT_KEY = "default_image_fetcher.max_source_image_bytes";
    
    @Autowired
    @Setter
    protected CacheManager appCacheManager;
    
    @Autowired
    @Setter
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

        Long maxSourceImageSize = Long.parseLong(configRepository.getGlobalConfiguration().value(SIZE_LIMIT_KEY));

        Cache cache = appCacheManager.getCache(CACHE_NAME);
        
        if (! cache.isKeyInCache(url)) {
            log.trace("Fetching " + url + " to cache");

            URL imageUrl = new URL(url);
            @Cleanup InputStream imageStream = imageUrl.openStream();
            @Cleanup InputStream boundedImageStream = ByteStreams.limit(imageStream, maxSourceImageSize + 1);
            byte[] imageBytes = IOUtils.toByteArray(boundedImageStream);
            if (imageBytes.length > maxSourceImageSize) {
                throw new RuntimeException("Image too large to be scaled by this system");
            }
            cache.put(new Element(url, imageBytes));
        }

        return (byte[])cache.get(url).getValue();
    }

    @Override
    public void clearCache(String url) {
        Cache cache = appCacheManager.getCache(CACHE);
        cache.remove(url);
    }

    @Override
    public void clearAllCache() {
        Cache cache = appCacheManager.getCache(CACHE);
        cache.removeAll();
    }
}
