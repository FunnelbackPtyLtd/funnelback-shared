package com.funnelback.publicui.test.web.interceptors;

import java.io.File;
import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletResponse;

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
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.web.interceptors.AccessRestrictionInterceptor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:test_data/spring/applicationContext.xml")
public class AccessRestrictionInterceptorTests {

	private static final String COLLECTION_ID = "interceptor";
	
	@Autowired
	private MockConfigRepository configRepository;
	
	private Config testCollectionConfig;
	
	@Autowired
	private AccessRestrictionInterceptor interceptor;
	
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	
	@Before
	public void before() throws FileNotFoundException {
		configRepository.removeAllCollections();
		testCollectionConfig = new NoOptionsConfig(new File("test_data/dummy-search_home"), COLLECTION_ID);
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
	public void testNoRestriction() throws Exception {
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, DefaultValues.NO_RESTRICTION);
		Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Response status should be unchanged", -1, response.getStatus());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
	}
	
	@Test
	public void testNoAccessNoAlternate() throws Exception {
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, DefaultValues.NO_ACCESS);
		testCollectionConfig.setValue(Keys.ACCESS_ALTERNATE, null);
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
	}
	
	@Test
	public void testNoAccessAccessAlternate() throws Exception {
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, DefaultValues.NO_ACCESS);
		testCollectionConfig.setValue(Keys.ACCESS_ALTERNATE, "alternate_collection");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertTrue("Redirect URL should point to alternate collection", response.getRedirectedUrl().contains(RequestParameters.COLLECTION + "=alternate_collection"));
	}

	@Test
	public void testIPBasedRestriction() throws Exception {
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "1.2");
		testCollectionConfig.setValue(Keys.ACCESS_ALTERNATE, null);
		Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
		
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "10.9");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
		response.setStatus(-1);		
		
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "3.4");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
	}
	
	@Test
	public void testHostnameBasedRestriction() throws Exception {
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "host.com");
		testCollectionConfig.setValue(Keys.ACCESS_ALTERNATE, null);
		Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());

		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "bad.com");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
		response.setStatus(-1);
		
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "remote");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
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
	
}
