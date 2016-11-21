package com.funnelback.publicui.test.search.service.image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.image.DefaultImageFetcher;
import com.google.common.io.Files;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class DefaultImageFetcherTest {

    @Test
    public void testNotOverSizeLimit() throws IOException {
        Integer sizeLimit = 20;
        String content = "abcdefghij";
        
        DefaultImageFetcher fetcher = getMockedDefaultImageFetcher(sizeLimit);

        File testContent = File.createTempFile(DefaultImageFetcherTest.class.getSimpleName(), "");
        testContent.deleteOnExit();
        Files.write(content.getBytes(StandardCharsets.US_ASCII), testContent);
        
        byte[] fetchedContent = fetcher.fetch(testContent.toURI().toString());
        
        Assert.assertEquals(content, new String(fetchedContent));
    }

    @Test(expected=RuntimeException.class)
    public void testOverSizeLimit() throws IOException {
        Integer sizeLimit = 5;
        String content = "abcdefghij";
        
        DefaultImageFetcher fetcher = getMockedDefaultImageFetcher(sizeLimit);

        File testContent = File.createTempFile(DefaultImageFetcherTest.class.getSimpleName(), "");
        testContent.deleteOnExit();
        Files.write(content.getBytes(StandardCharsets.US_ASCII), testContent);
        
        byte[] fetchedContent = fetcher.fetch(testContent.toURI().toString());
        
        Assert.assertEquals(content, new String(fetchedContent));
    }

    private DefaultImageFetcher getMockedDefaultImageFetcher(Integer sizeLimit) {
        DefaultImageFetcher fetcher = new DefaultImageFetcher();
        
        // Give a small value (10) for the size limit
        Config mockConfig = Mockito.mock(Config.class);
        Mockito.when(mockConfig.value(DefaultImageFetcher.SIZE_LIMIT_KEY)).thenReturn(sizeLimit.toString());
        ConfigRepository repository = Mockito.mock(ConfigRepository.class);
        Mockito.when(repository.getGlobalConfiguration()).thenReturn(mockConfig);
        fetcher.setConfigRepository(repository);
        
        // Don't use any caching for the test
        Cache cache = new Cache("testCache", 5, false, true, Long.MAX_VALUE, Long.MAX_VALUE);
        cache.initialise();
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Mockito.when(cacheManager.getCache(DefaultImageFetcher.CACHE_NAME)).thenReturn(cache);
        fetcher.setAppCacheManager(cacheManager);
        return fetcher;
    }
}
