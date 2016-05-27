package com.funnelback.publicui.test.search.web.interceptors;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.interceptors.DenyIfDlsIsOnInterceptor;
import com.funnelback.publicui.test.mock.MockConfigRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DenyIfDlsIsOnInterceptorTests {

    private static final String COLLECTION_ID = "interceptor";
    
    @Autowired
    private MockConfigRepository configRepository;
    
    private Config testCollectionConfig;
    
    @Autowired
    private DenyIfDlsIsOnInterceptor interceptor;
    
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    @Before
    public void before() throws FileNotFoundException {
        configRepository.removeAllCollections();
        testCollectionConfig = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), COLLECTION_ID);
        testCollectionConfig.setValue(Keys.COLLECTION_TYPE, "meta");
        
        configRepository.addCollection(new Collection(COLLECTION_ID, testCollectionConfig));
        
        request = new MockHttpServletRequest();
        request.setParameter(RequestParameters.COLLECTION, COLLECTION_ID);
        request.setQueryString(RequestParameters.COLLECTION + "=" + COLLECTION_ID + "&query=dummy");
        request.setRemoteAddr("1.2.3.4");
        request.setRemoteHost("remote.host.com");
        
        response = new MockHttpServletResponse();
        response.setStatus(-1);
    }
    
    
    @Test
    public void testMetaCollectionNoRestrection() throws Exception {
        testCollectionConfig.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER,"");
        testCollectionConfig.setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE ,"disabled");
        testCollectionConfig.setValue(Keys.COLLECTION_TYPE, "meta");
        
        Collection metaColl = new Collection(COLLECTION_ID, testCollectionConfig);
        metaColl.setMetaComponents(new String[]{"sub","doesnotexist"});
        
        configRepository.addCollection(metaColl);

        Config config = mock(Config.class);
        when(config.value(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "")).thenReturn("");
        when(config.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, "")).thenReturn("");
        when(config.getCollectionName()).thenReturn("sub");
        
        Collection sub = new Collection("sub", config);
        
        
        
        configRepository.addCollection(sub);
        
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
    }
    
    @Test
    public void testMetaCollectionWithRestriction() throws Exception {
        testCollectionConfig.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER,"bar");
        testCollectionConfig.setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE ,"true");
        testCollectionConfig.setValue(Keys.COLLECTION_TYPE, "meta");
        
        Collection metaColl = new Collection(COLLECTION_ID, testCollectionConfig);
        metaColl.setMetaComponents(new String[]{"sub","doesnotexist"});
        
        configRepository.addCollection(metaColl);

        Config config = mock(Config.class);
        when(config.value(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "")).thenReturn("");
        when(config.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, "")).thenReturn("");
        when(config.getCollectionName()).thenReturn("sub");
        
        Collection sub = new Collection("sub", config);
        
        
        
        configRepository.addCollection(sub);
        
        Assert.assertFalse("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
    }
    
    @Test
    public void testNoRestriction() throws Exception {
        testCollectionConfig.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER,"");
        testCollectionConfig.setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE ,"disabled");
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
    }
    
    @Test
    public void testSecurityEarlyBindingIsOn() throws Exception {
        testCollectionConfig.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER,"Trim");
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
    }
    
    @Test
    public void testDLS() throws Exception {
        testCollectionConfig.setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE,"custom,minresults");
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
    }

    @Test
    public void testBoth() throws Exception {
        testCollectionConfig.setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE,"custom,minresults");
        testCollectionConfig.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER,"Trim");
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
    }
    
    
    @Test
    public void testNoCollectionParameterShouldSkipInterceptor() throws Exception {
        request.removeParameter(RequestParameters.COLLECTION);
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
    }
    
    @Test
    public void testInvalidCollectionParameterShouldSkipInterceptor() throws Exception {
        request.setParameter(RequestParameters.COLLECTION, "invalid-collection");
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));    
    }
    
}
