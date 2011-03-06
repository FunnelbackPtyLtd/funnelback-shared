package com.funnelback.publicui.test.web.filters;

import java.util.HashMap;
import java.util.Map;

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.service.ConfigRepository.GlobalConfiguration;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.web.filters.HostnameRestrictionFilter;
import com.funnelback.publicui.web.filters.RequestParametersTransformWrapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class HostnameRestrictionFilterTests {

	@Autowired
	private HostnameRestrictionFilter filter;
	
	@Autowired
	private MockConfigRepository configRepository;
	
	@Before
	public void before() {
		Map<String, String> config = new HashMap<String, String>();
		config.put("dummy.host", "dummy1,dummy2");
		config.put("no-collection.host", null);
		config.put("empty-collection.host", "");
		configRepository.getGlobalConfigs().clear();
		configRepository.getGlobalConfigs().put(GlobalConfiguration.DNSAliases, config);
	}
	
	@Test
	public void testInit() throws ServletException {
		// These methods are unused but this will make Cobertura happy
		filter.init(null);
		filter.destroy();
	}

	@Test
	public void testNoCollection() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setServerName("dummy.host");
		
		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);
		
		Assert.assertTrue(chain.getRequest() instanceof MockHttpServletRequest);
		Assert.assertEquals(0, chain.getRequest().getParameterMap().size());
	}
	
	@Test
	public void testNoDnsAliases() throws Exception {
		configRepository.getGlobalConfigs().clear();
		
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("collection", "dummy1");
		req.setParameter("query", "hello");
		req.setServerName("dummy.host");
		
		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);
		
		Assert.assertTrue(chain.getRequest() instanceof MockHttpServletRequest);
		Assert.assertEquals(2, chain.getRequest().getParameterMap().size());
		Assert.assertEquals("dummy1", chain.getRequest().getParameter("collection"));
		Assert.assertEquals("hello", chain.getRequest().getParameter("query"));
	}
	
	@Test
	public void testAuthorizedCollection() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("collection", "dummy2");
		req.setParameter("query", "hello");
		req.setServerName("dummy.host");

		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);
		
		Assert.assertTrue(chain.getRequest() instanceof MockHttpServletRequest);
		Assert.assertEquals(2, chain.getRequest().getParameterMap().size());
		Assert.assertEquals("dummy2", chain.getRequest().getParameter("collection"));
		Assert.assertEquals("hello", chain.getRequest().getParameter("query"));
	}

	@Test
	public void testUnauthorizedCollection() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("collection", "dummy3");
		req.setParameter("query", "hello");
		req.setServerName("dummy.host");

		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);
		
		Assert.assertTrue(chain.getRequest() instanceof RequestParametersTransformWrapper);
		Assert.assertEquals(2, chain.getRequest().getParameterMap().size());
		Assert.assertEquals("dummy1", chain.getRequest().getParameter("collection"));
		Assert.assertEquals("hello", chain.getRequest().getParameter("query"));
	}
	
	@Test
	public void testNoCollectionForThisHost() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("collection", "dummy3");
		req.setParameter("query", "hello");
		req.setServerName("no-collection.host");

		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);
		
		Assert.assertTrue(chain.getRequest() instanceof MockHttpServletRequest);
		Assert.assertEquals(2, chain.getRequest().getParameterMap().size());
		Assert.assertEquals("dummy3", chain.getRequest().getParameter("collection"));
		Assert.assertEquals("hello", chain.getRequest().getParameter("query"));
		
		req.setServerName("empty-collection.host");
		
		chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);

		Assert.assertTrue(chain.getRequest() instanceof MockHttpServletRequest);
		Assert.assertEquals(2, chain.getRequest().getParameterMap().size());
		Assert.assertEquals("dummy3", chain.getRequest().getParameter("collection"));
		Assert.assertEquals("hello", chain.getRequest().getParameter("query"));
	}
	
}
