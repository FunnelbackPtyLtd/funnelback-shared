package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.Geolocation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockGeolocator;


public class GeolocationTests {

	private Geolocation processor;
	private Config conf;
	
	private SearchTransaction getProcessedTransaction(Config conf) throws InputProcessorException {
		SearchQuestion question = new SearchQuestion();
		question.setCollection(new Collection("dummy", conf));
		
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
	public void testMissing() throws InputProcessorException, EnvironmentVariableException, FileNotFoundException {
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
	public void testGeolocationEnabledAndOriginSet() throws FileNotFoundException, InputProcessorException {
		conf.setValue(Keys.ModernUI.GEOLOCATION_ENABLED, "true");
        conf.setValue(Keys.ModernUI.GEOLOCATION_SET_ORIGIN, "true");
		
		SearchTransaction st = getProcessedTransaction(conf);
		
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
	public void originAlreadySetAndOriginSet() throws InputProcessorException, FileNotFoundException {
		conf.setValue(Keys.ModernUI.GEOLOCATION_ENABLED, "true");
        conf.setValue(Keys.ModernUI.GEOLOCATION_SET_ORIGIN, "true");
		//Set origin
        SearchQuestion question = new SearchQuestion();
		question.setCollection(new Collection("dummy", conf));
		
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
	public void originAlreadySetAndOriginNotSet() throws InputProcessorException, FileNotFoundException {
		conf.setValue(Keys.ModernUI.GEOLOCATION_ENABLED, "true");
        conf.setValue(Keys.ModernUI.GEOLOCATION_SET_ORIGIN, "false");
		//Set origin
        SearchQuestion question = new SearchQuestion();
		question.setCollection(new Collection("dummy", conf));
		
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
	public void testGeolocationEnabled() throws FileNotFoundException, InputProcessorException{
		conf.setValue(Keys.ModernUI.GEOLOCATION_ENABLED, "true");
        conf.setValue(Keys.ModernUI.GEOLOCATION_SET_ORIGIN, "false");
        
        SearchTransaction st = getProcessedTransaction(conf);
		
		if (null != st.getQuestion().getAdditionalParameters())
			Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNotNull(st.getQuestion().getLocation());
        Assert.assertEquals((float) 20, st.getQuestion().getLocation().getLatitude(), 0.0); 
        Assert.assertEquals((float) 100, st.getQuestion().getLocation().getLongitude(), 0.0); 
	}
	
	@Test
	public void testGeolocationDisabledAndOrginEnabled() throws FileNotFoundException, InputProcessorException{
		conf.setValue(Keys.ModernUI.GEOLOCATION_ENABLED, "false");
        conf.setValue(Keys.ModernUI.GEOLOCATION_SET_ORIGIN, "true");
        
        SearchTransaction st = getProcessedTransaction(conf);
        
        if (null != st.getQuestion().getAdditionalParameters())
        	Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNull(st.getQuestion().getLocation());
	}
	
	@Test
	public void testBothDisbled() throws FileNotFoundException, InputProcessorException {
		//Keys.ModernUI.GEOLOCATION_ENABLED == false && Keys.ModernUI.GEOLOCATION_SET_ORIGIN == false
        conf.setValue(Keys.ModernUI.GEOLOCATION_ENABLED, "false");
        conf.setValue(Keys.ModernUI.GEOLOCATION_SET_ORIGIN, "false");
        SearchTransaction st = getProcessedTransaction(conf);
        
        if (null != st.getQuestion().getAdditionalParameters())
        	Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.ORIGIN));
        Assert.assertNull(st.getQuestion().getLocation());
	}

}
