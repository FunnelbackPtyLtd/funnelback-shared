package com.funnelback.publicui.test.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.funnelback.common.config.Collection;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.testutils.TestUtils;
import com.funnelback.publicui.utils.UrlStatus;
import com.funnelback.publicui.utils.UrlStatus.MatchResult;

public class UrlStatusTest {

    @Rule public TestName name = new TestName();
    
    @Test
    public void testMetaCollection() throws Exception {
        File searchHome = TestUtils.getWritableSearchHome(UrlStatusTest.class.getSimpleName(), name.getMethodName());
        {
            Map<String, String> params = new HashMap<>();
            params.put(Keys.COLLECTION_TYPE, Collection.Type.web.toString());
            params.put("include_patterns" , "http://david-hawking.net/");
            TestUtils.createVeryBasicCollection(searchHome, "subcoll1", params);
            
        }
        {
            Map<String, String> params = new HashMap<>();
            params.put(Keys.COLLECTION_TYPE, Collection.Type.web.toString());
            params.put("include_patterns" , "http://david.net/");
            TestUtils.createVeryBasicCollection(searchHome, "subcoll2", params);
            
        }
        {
            Map<String, String> metaParams = new HashMap<>();
            metaParams.put(Keys.COLLECTION_TYPE, Collection.Type.meta.toString());
            metaParams.put(com.funnelback.config.keys.Keys.CollectionKeys.Meta.META_COMPONENTS.getKey(), "subcoll1,subcoll2");
            TestUtils.createVeryBasicCollection(searchHome, "parent", metaParams);
        }
        
        Assert.assertEquals(MatchResult.MATCHES, 
            UrlStatus.UrlMatchesCrawlerIncludeExcludePattern(searchHome, "parent", "http://david-hawking.net/"));
        
        Assert.assertEquals(MatchResult.MATCHES, 
            UrlStatus.UrlMatchesCrawlerIncludeExcludePattern(searchHome, "parent", "http://david.net/"));
        
        Assert.assertEquals(MatchResult.FAILS, 
            UrlStatus.UrlMatchesCrawlerIncludeExcludePattern(searchHome, "parent", "http://tim-jones.net/"));    
    }
    
    @Test
    public void testMetaCollectionWithPush() throws Exception {
        File searchHome = TestUtils.getWritableSearchHome(UrlStatusTest.class.getSimpleName(), name.getMethodName());
        {
            Map<String, String> params = new HashMap<>();
            params.put(Keys.COLLECTION_TYPE, Collection.Type.push2.toString());
            params.put("include_patterns" , "http://david-hawking.net/");
            TestUtils.createVeryBasicCollection(searchHome, "subcoll1", params);
            
        }
        {
            Map<String, String> params = new HashMap<>();
            params.put(Keys.COLLECTION_TYPE, Collection.Type.web.toString());
            params.put("include_patterns" , "http://david.net/");
            TestUtils.createVeryBasicCollection(searchHome, "subcoll2", params);
            
        }
        {
            Map<String, String> metaParams = new HashMap<>();
            metaParams.put(Keys.COLLECTION_TYPE, Collection.Type.meta.toString());
            metaParams.put(com.funnelback.config.keys.Keys.CollectionKeys.Meta.META_COMPONENTS.getKey(), "subcoll1,subcoll2");
            TestUtils.createVeryBasicCollection(searchHome, "parent", metaParams);
        }
        
        Assert.assertEquals(MatchResult.NOT_RELEVANT, 
            UrlStatus.UrlMatchesCrawlerIncludeExcludePattern(searchHome, "parent", "http://david-hawking.net/"));
        
        Assert.assertEquals(MatchResult.MATCHES, 
            UrlStatus.UrlMatchesCrawlerIncludeExcludePattern(searchHome, "parent", "http://david.net/"));
        
        Assert.assertEquals(MatchResult.NOT_RELEVANT, 
            UrlStatus.UrlMatchesCrawlerIncludeExcludePattern(searchHome, "parent", "http://tim-jones.net/"));    
    }
    
    
}
