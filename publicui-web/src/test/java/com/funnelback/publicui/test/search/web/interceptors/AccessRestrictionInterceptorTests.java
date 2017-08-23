package com.funnelback.publicui.test.search.web.interceptors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.OptionAndValue;
import com.funnelback.config.data.environment.ConfigEnvironment;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.config.data.service.ServiceConfigData;
import com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.config.option.ConfigOptionDefinition;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.exception.InvalidCollectionException;
import com.funnelback.publicui.search.web.interceptors.AccessRestrictionInterceptor;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.google.common.collect.Maps;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class AccessRestrictionInterceptorTests {

    private static final String COLLECTION_ID = "interceptor";

    @Autowired
    private MockConfigRepository configRepository;

    private ServiceConfig testServiceConfig;

    @Autowired
    private AccessRestrictionInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void before() throws FileNotFoundException, ProfileNotFoundException {
        configRepository.removeAllCollections();

        testServiceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        configRepository.setServiceConfig(testServiceConfig);

        request = new MockHttpServletRequest();
        request.setParameter(RequestParameters.COLLECTION, COLLECTION_ID);
        request.setParameter(RequestParameters.QUERY, "dummy");
        request.setQueryString(RequestParameters.COLLECTION + "=" + COLLECTION_ID + "&query=dummy");
        request.setRemoteAddr("1.2.3.4");
        request.setRemoteHost("remote.host.com");

        response = newResponse();
        
        interceptor.setConfigRepository(configRepository);
    }
    
    private static MockHttpServletResponse newResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(-1);
        return response;
    }
    
    @Test
    public void testOldIPPattern(){
        Assert.assertTrue(AccessRestrictionInterceptor.getOLD_IP_PATTERN().matcher("1.2.3.4").matches());
        Assert.assertTrue(AccessRestrictionInterceptor.getOLD_IP_PATTERN().matcher("1.2").matches());
        Assert.assertTrue(AccessRestrictionInterceptor.getOLD_IP_PATTERN().matcher("1.2.").matches());
        Assert.assertTrue(AccessRestrictionInterceptor.getOLD_IP_PATTERN().matcher("255.255.255.255").matches());
        Assert.assertFalse(AccessRestrictionInterceptor.getOLD_IP_PATTERN().matcher("1.2.3.4d").matches());
        Assert.assertFalse(AccessRestrictionInterceptor.getOLD_IP_PATTERN().matcher("FE80:0000:0000:0000:0202:B3FF:FE1E:8320").matches());
        Assert.assertFalse(AccessRestrictionInterceptor.getOLD_IP_PATTERN().matcher("1.2.3.4/8").matches());
    }
    

    @Test
    public void testNoRestriction() throws Exception {
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of(DefaultValues.NO_RESTRICTION));
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Response status should be unchanged", -1, response.getStatus());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test
    public void testNoAccessNoAlternate() throws Exception {
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of(DefaultValues.NO_ACCESS));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.empty());
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals("access.profile.denied", response.getContentAsString());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test
    public void testDefaultProfileFallback() throws Exception {
        ConfigRepository configRepository = Mockito.mock(ConfigRepository.class);
        interceptor.setConfigRepository(configRepository);
        
        request.setParameter("profile", "doesntexist");

        Mockito.when(configRepository.getServiceConfig(COLLECTION_ID, "doesntexist")).thenThrow(ProfileNotFoundException.class);
        Mockito.when(configRepository.getServiceConfig(COLLECTION_ID, "_default")).thenReturn(testServiceConfig);
        
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of(DefaultValues.NO_ACCESS));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.empty());
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals("access.profile.denied", response.getContentAsString());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test
    public void testEmptyAccessNoAlternate() throws Exception {
        // This is what currently actually happens, as access_alternate is set to blank in collection.cfg.default
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of(DefaultValues.NO_ACCESS));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.of(""));
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals("access.profile.denied", response.getContentAsString());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test
    public void testNoAccessAccessAlternate() throws Exception {
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of(DefaultValues.NO_ACCESS));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.of("alternate_collection"));
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertTrue("Redirect URL should point to alternate collection", response.getRedirectedUrl().contains(RequestParameters.COLLECTION + "=alternate_collection"));
        Assert.assertTrue("Redirect URL should have same query", response.getRedirectedUrl().contains("&query=dummy"));
    }

    @Test
    public void testIPBasedRestrictionAllowedIp() throws Exception {
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("1.2.3.4/8"));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.empty());
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test
    public void testIPBasedRestrictionNotAllowedIp() throws Exception {
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("10.7.6.5/8"));
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals("access.profile.denied", response.getContentAsString());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }
    

    @Test
    public void testHostnameBasedRestriction() throws Exception {
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("host.com"));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.empty());
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
        response = new MockHttpServletResponse();

        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("bad.com"));
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals("access.profile.denied", response.getContentAsString());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
        response = new MockHttpServletResponse();

        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("remote"));
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals("access.profile.denied", response.getContentAsString());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test
    public void testNoCollectionParameterShouldSkipInterceptor() throws Exception {
        request.removeParameter(RequestParameters.COLLECTION);
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test
    public void testInvalidCollectionParameterShouldSkipInterceptor() throws Exception {
        request.setParameter(RequestParameters.COLLECTION, "invalid-collection");
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());        
    }
    
    /**
     * Test things like 10.9.
     */
    @Test
    public void testOldIPRangeStyleUsed() throws Exception{
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("1.2.3.4"));
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals("access.profile.denied", response.getContentAsString());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }
    
    @Test
    public void testGetConnectingIp() throws Exception {
        request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "127.0.0.1,150.203.239.15,10.7.6.17");
        testServiceConfig.set(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR, true);
        testServiceConfig.set(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES, Optional.of("10.7.6.0/24"));
        Assert.assertEquals("150.203.239.15", 
                interceptor.getConnectingIp(request, testServiceConfig));
        
    }
    
    @Test
    public void testGetConnectingIpUseConnectingIP() throws Exception {
        request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "10.7.6.17");
        testServiceConfig.set(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR, true);
        testServiceConfig.set(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES, Optional.of("10.7.6.0/24"));
        Assert.assertEquals("1.2.3.4", 
                interceptor.getConnectingIp(request, testServiceConfig));
    }
    
    @Test
    public void testGetConnectingIpPreferConnecxtingIP() throws Exception {
        request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "10.7.6.17,150.203.239.15");
        testServiceConfig.set(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR, false);
        testServiceConfig.set(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES, Optional.of("10.7.6.0/24"));
        Assert.assertEquals("1.2.3.4", 
                interceptor.getConnectingIp(request, testServiceConfig));
    }
    
    @Test
    public void testXForwardedForAllowed() throws Exception {
        request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "127.0.0.1,150.203.239.15,10.7.6.17");
        testServiceConfig.set(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR, true);
        testServiceConfig.set(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES, Optional.of("10.7.6.0/24"));
        
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("150.203.239.0/24"));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.empty());
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
        
    }
    
    @Test
    public void testXForwardedForBlocked() throws Exception {
        request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "127.0.0.1,150.203.239.15,10.7.6.17");
        testServiceConfig.set(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR, true);
        testServiceConfig.set(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES, Optional.of("10.7.6.0/24"));
        
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("100.100.239.0/24"));
        Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals("access.profile.denied", response.getContentAsString());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }
    
    @Test
    public void testGetConnectingIpXForwardedForEmpty() throws Exception {
        request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "");
        testServiceConfig.set(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR, false);
        testServiceConfig.set(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES, Optional.of("10.7.6.0/24"));
        Assert.assertEquals("1.2.3.4", 
                interceptor.getConnectingIp(request, testServiceConfig));
    }
    
    @Test
    public void testXForwardedAllowedButUsedConnectingIP() throws Exception {
        request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "127.0.0.1,150.203.239.15,10.7.6.17");
        testServiceConfig.set(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR, true);
        testServiceConfig.set(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES, Optional.of("10.7.6.0/24,127.0.0.1/8,150.203.239.15/8"));
        
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("1.2.3.4/24"));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.empty());
        Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }
    
    @Test
    public void testXForwardedAllowedButUsedConnectingIP2() throws Exception {
        request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "150.203.238.14,150.203.239.15");
        testServiceConfig.set(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR, true);
        testServiceConfig.set(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES, Optional.of("150.203.239.0/24,150.203.238.0/24"));
        
        testServiceConfig.set(FrontEndKeys.ACCESS_RESTRICTION, Optional.of("150.203.238.0/24"));
        testServiceConfig.set(FrontEndKeys.ACCESS_ALTERNATE, Optional.empty());
        request.setRemoteAddr("127.0.0.1");
        Assert.assertFalse("Intercepter should block", interceptor.preHandle(request, response, null));
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test
    public void testPassOnMissingCollection() throws Exception {
        ConfigRepository configRepository = Mockito.mock(ConfigRepository.class);
        interceptor.setConfigRepository(configRepository);
        
        Mockito.when(configRepository.getServiceConfig(Mockito.anyString(), Mockito.anyString())).thenThrow(ProfileNotFoundException.class);
        Mockito.when(configRepository.getCollection(Mockito.anyString())).thenReturn(null);
        
        Assert.assertTrue("Interceptor should permit processing", interceptor.preHandle(request, response, null));
        Assert.assertEquals("Response status should be unchanged", -1, response.getStatus());
        Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
    }

    @Test(expected=InvalidCollectionException.class)
    public void testErrorOnInvalidCollection() throws Exception {
        ConfigRepository configRepository = Mockito.mock(ConfigRepository.class);
        interceptor.setConfigRepository(configRepository);
        
        Mockito.when(configRepository.getServiceConfig(Mockito.anyString(), Mockito.anyString())).thenThrow(ProfileNotFoundException.class);
        Mockito.when(configRepository.getCollection(Mockito.anyString())).thenReturn(Mockito.mock(Collection.class));

        interceptor.preHandle(request, response, null);
    }

}
