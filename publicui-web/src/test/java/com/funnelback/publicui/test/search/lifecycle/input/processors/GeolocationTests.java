package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.File;
import java.io.FileNotFoundException;

import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.model.collection.Profile;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.Geolocation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockGeolocator;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

public class GeolocationTests {

    private Geolocation processor;
    private Config conf;

    private SearchTransaction getProcessedTransaction(ServiceConfig serviceConfig) throws InputProcessorException {
        Profile profile = new Profile("_default");
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection("dummy", conf);
        collection.getProfiles().put("_default", profile);

        SearchQuestion question = new SearchQuestion();
        question.setCurrentProfile("_default");
        question.setCollection(collection);

        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        return st;
    }
    
    @Before
    public void before() throws FileNotFoundException {
        processor = new Geolocation();
        processor.setGeolocator(new MockGeolocator());
        conf = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "dummy");
    }
    
    @Test
    public void testMissing() throws InputProcessorException, EnvironmentVariableException {
        //No transaction
        this.processor.processInput(null);
        
        //no question
        this.processor.processInput(new SearchTransaction(null, null));
        
        //no collection
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        this.processor.processInput(st);
        if (null != st.getQuestion().getAdditionalParameters())
            Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey("origin"));
        Assert.assertNull(st.getQuestion().getLocation());
        
        
    }
    
    @Test
    public void testGeolocationEnabledAndOriginSet() throws InputProcessorException {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.ENABLED, true);
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.SET_ORIGIN, true);
        
        SearchTransaction st = getProcessedTransaction(serviceConfig);
        
        Assert.assertNotNull(st.getQuestion().getAdditionalParameters());
        Assert.assertTrue(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNotNull(st.getQuestion().getLocation());
        Assert.assertEquals((float) 20, st.getQuestion().getLocation().getLatitude(), 0.0); 
        Assert.assertEquals((float) 100, st.getQuestion().getLocation().getLongitude(), 0.0); 
        
        //
       
    }
    
    /**
     * Check to see what happens to the CGI param if origin is already set
     * @throws InputProcessorException 
     * @throws FileNotFoundException 
     */
    @Test
    public void originAlreadySetAndOriginSet() throws InputProcessorException {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.ENABLED, true);
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.SET_ORIGIN, true);

        Profile profile = new Profile("_default");
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection("dummy", conf);
        collection.getProfiles().put("_default", profile);

        SearchQuestion question = new SearchQuestion();
        question.setCurrentProfile("_default");
        question.setCollection(collection);

        SearchTransaction st = new SearchTransaction(question, null);
        st.getQuestion().getAdditionalParameters().put(RequestParameters.ORIGIN, null);
        processor.processInput(st);
        
        Assert.assertNotNull(st.getQuestion().getAdditionalParameters());
        Assert.assertTrue(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNull("The processor should have left this as null", st.getQuestion().getAdditionalParameters().get(RequestParameters.ORIGIN));
        Assert.assertNotNull(st.getQuestion().getLocation());
        Assert.assertEquals((float) 20, st.getQuestion().getLocation().getLatitude(), 0.0); 
        Assert.assertEquals((float) 100, st.getQuestion().getLocation().getLongitude(), 0.0);
    }
    
    /**
     * Check to see what happens to the CGI param if origin is already set
     * @throws InputProcessorException 
     * @throws FileNotFoundException 
     */
    @Test
    public void originAlreadySetAndOriginNotSet() throws InputProcessorException {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.ENABLED, true);
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.SET_ORIGIN, false);

        Profile profile = new Profile("_default");
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection("dummy", conf);
        collection.getProfiles().put("_default", profile);

        SearchQuestion question = new SearchQuestion();
        question.setCurrentProfile("_default");
        question.setCollection(collection);

        SearchTransaction st = new SearchTransaction(question, null);
        st.getQuestion().getAdditionalParameters().put(RequestParameters.ORIGIN, null);
        processor.processInput(st);

        Assert.assertNotNull(st.getQuestion().getAdditionalParameters());
        Assert.assertTrue(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNull("The processor should have left this as null", st.getQuestion().getAdditionalParameters().get(RequestParameters.ORIGIN));
        Assert.assertNotNull(st.getQuestion().getLocation());
        Assert.assertEquals((float) 20, st.getQuestion().getLocation().getLatitude(), 0.0); 
        Assert.assertEquals((float) 100, st.getQuestion().getLocation().getLongitude(), 0.0);
    }
    
    
    
    @Test 
    public void testGeolocationEnabled() throws InputProcessorException{
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.ENABLED, true);
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.SET_ORIGIN, false);

        SearchTransaction st = getProcessedTransaction(serviceConfig);
        
        if (null != st.getQuestion().getAdditionalParameters())
            Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNotNull(st.getQuestion().getLocation());
        Assert.assertEquals((float) 20, st.getQuestion().getLocation().getLatitude(), 0.0); 
        Assert.assertEquals((float) 100, st.getQuestion().getLocation().getLongitude(), 0.0); 
    }
    
    @Test
    public void testGeolocationDisabledAndOrginEnabled() throws InputProcessorException{
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.ENABLED, false);
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.SET_ORIGIN, true);

        SearchTransaction st = getProcessedTransaction(serviceConfig);
        
        if (null != st.getQuestion().getAdditionalParameters())
            Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNull(st.getQuestion().getLocation());
    }
    
    @Test
    public void testBothDisbled() throws InputProcessorException {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.ENABLED, false);
        serviceConfig.set(FrontEndKeys.ModernUi.GeoLocation.SET_ORIGIN, false);

        SearchTransaction st = getProcessedTransaction(serviceConfig);
        
        if (null != st.getQuestion().getAdditionalParameters())
            Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNull(st.getQuestion().getLocation());
    }

}
