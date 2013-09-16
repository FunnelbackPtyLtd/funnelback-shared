package com.funnelback.publicui.test.search.web.interceptors;

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
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.interceptors.AccessRestrictionInterceptor;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
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
		testCollectionConfig = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), COLLECTION_ID);
		configRepository.addCollection(new Collection(COLLECTION_ID, testCollectionConfig));

		request = new MockHttpServletRequest();
		request.setParameter(RequestParameters.COLLECTION, COLLECTION_ID);
		request.setParameter(RequestParameters.QUERY, "dummy");
		request.setQueryString(RequestParameters.COLLECTION + "=" + COLLECTION_ID + "&query=dummy");
		request.setRemoteAddr("1.2.3.4");
		request.setRemoteHost("remote.host.com");

		response = newResponse();
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
		Assert.assertEquals("access.collection.denied", response.getContentAsString());
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
	public void testIPBasedRestrictionAllowedIp() throws Exception {
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "1.2.3.4/8");
		testCollectionConfig.setValue(Keys.ACCESS_ALTERNATE, null);
		Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
	}

	@Test
	public void testIPBasedRestrictionNotAllowedIp() throws Exception {
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "10.7.6.5/8");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals("access.collection.denied", response.getContentAsString());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
	}
	

	@Test
	public void testHostnameBasedRestriction() throws Exception {
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "host.com");
		testCollectionConfig.setValue(Keys.ACCESS_ALTERNATE, null);
		Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
		response = new MockHttpServletResponse();

		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "bad.com");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals("access.collection.denied", response.getContentAsString());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
		response = new MockHttpServletResponse();

		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "remote");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals("access.collection.denied", response.getContentAsString());
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
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "1.2.3.4");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals("access_restriction in this collection's collection.cfg is misconfigured, IP ranges must be in CIDR format", response.getContentAsString());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
	}
	
	@Test
	public void testGetConnectingIp() throws Exception {
		request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "127.0.0.1,150.203.239.15,10.7.6.17");
		this.testCollectionConfig.setValue("access_restriction.preffer_x_forwarded_for", "true");
		this.testCollectionConfig.setValue("access_restriction.ignored_ip_ranges", "10.7.6.0/24");
		Assert.assertEquals("150.203.239.15", 
				interceptor.getConnectingIp(request, configRepository.getCollection(COLLECTION_ID)));
		
	}
	
	@Test
	public void testGetConnectingIpUseConnectingIP() throws Exception {
		request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "10.7.6.17");
		this.testCollectionConfig.setValue("access_restriction.preffer_x_forwarded_for", "true");
		this.testCollectionConfig.setValue("access_restriction.ignored_ip_ranges", "10.7.6.0/24");
		Assert.assertEquals("1.2.3.4", 
				interceptor.getConnectingIp(request, configRepository.getCollection(COLLECTION_ID)));
	}
	
	@Test
	public void testGetConnectingIpPreferConnecxtingIP() throws Exception {
		request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "10.7.6.17,150.203.239.15");
		this.testCollectionConfig.setValue("access_restriction.preffer_x_forwarded_for", "false");
		this.testCollectionConfig.setValue("access_restriction.ignored_ip_ranges", "10.7.6.0/24");
		Assert.assertEquals("1.2.3.4", 
				interceptor.getConnectingIp(request, configRepository.getCollection(COLLECTION_ID)));
	}
	
	@Test
	public void testXForwardedForAllowed() throws Exception {
		request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "127.0.0.1,150.203.239.15,10.7.6.17");
		this.testCollectionConfig.setValue("access_restriction.prefer_x_forwarded_for", "true");
		this.testCollectionConfig.setValue("access_restriction.ignored_ip_ranges", "10.7.6.0/24");
		
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "150.203.239.0/24");
		testCollectionConfig.setValue(Keys.ACCESS_ALTERNATE, null);
		Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
		
	}
	
	@Test
	public void testXForwardedForBlocked() throws Exception {
		request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "127.0.0.1,150.203.239.15,10.7.6.17");
		this.testCollectionConfig.setValue("access_restriction.preffer_x_forwarded_for", "true");
		this.testCollectionConfig.setValue("access_restriction.ignored_ip_ranges", "10.7.6.0/24");
		
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "100.100.239.0/24");
		Assert.assertFalse("Interceptor should block processing", interceptor.preHandle(request, response, null));
		Assert.assertEquals("Access should be denied", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals("access.collection.denied", response.getContentAsString());
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
	}
	
	@Test
	public void testGetConnectingIpXForwardedForEmpty() throws Exception {
		request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "");
		this.testCollectionConfig.setValue("access_restriction.preffer_x_forwarded_for", "false");
		this.testCollectionConfig.setValue("access_restriction.ignored_ip_ranges", "10.7.6.0/24");
		Assert.assertEquals("1.2.3.4", 
				interceptor.getConnectingIp(request, configRepository.getCollection(COLLECTION_ID)));
	}
	
	@Test
	public void testXForwardedAllowedButUsedConnectingIP() throws Exception {
		request.addHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR, "127.0.0.1,150.203.239.15,10.7.6.17");
		this.testCollectionConfig.setValue("access_restriction.preffer_x_forwarded_for", "true");
		this.testCollectionConfig.setValue("access_restriction.ignored_ip_ranges", "10.7.6.0/24,127.0.0.1/8,150.203.239.15/8");
		
		testCollectionConfig.setValue(Keys.ACCESS_RESTRICTION, "1.2.3.4/24");
		testCollectionConfig.setValue(Keys.ACCESS_ALTERNATE, null);
		Assert.assertTrue("Interceptor shouldn't block processing", interceptor.preHandle(request, response, null));
		Assert.assertNull("Response shouldn't be redirected", response.getRedirectedUrl());
	}

}
