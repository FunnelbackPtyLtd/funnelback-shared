package com.funnelback.publicui.test.search.web.filters;

import java.io.FileNotFoundException;
import java.io.IOException;

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

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.paramtransform.ParamTransformRuleFactory;
import com.funnelback.publicui.search.web.filters.RequestParametersTransformFilter;
import com.funnelback.publicui.search.web.filters.RequestParametersTransformWrapper;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class RequestParametersTransformFilterTests {

	@Autowired
	private RequestParametersTransformFilter filter;
	
	@Autowired
	private MockConfigRepository configRepository;
	
	@Before
	public void before() throws FileNotFoundException, EnvironmentVariableException {		
		configRepository.removeAllCollections();
		configRepository.addCollection(new Collection("dummy", null));
		
		Collection c = new Collection("cgi-transform", new NoOptionsConfig("cgi-transform"));
		c.setParametersTransforms(
				ParamTransformRuleFactory.buildRules(new String[] {
						"coverage=abcnews => -scope",
						"coverage=abcnews => profile=news&clive=abc&clive=news&scope=/news"	
				}));
		configRepository.addCollection(c);
	}
	
	@Test
	public void testInit() throws ServletException {
		// These methods are unused but this will make Cobertura happy
		filter.init(null);
		filter.destroy();
	}
	
	@Test
	public void testNoTransform() throws IOException, ServletException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("collection", "dummy");
		req.setParameter("query", "hello");
		
		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);
		
		Assert.assertTrue(chain.getRequest() instanceof MockHttpServletRequest);
		Assert.assertEquals(2, chain.getRequest().getParameterMap().size());
		Assert.assertEquals("dummy", chain.getRequest().getParameter("collection"));
		Assert.assertEquals("hello", chain.getRequest().getParameter("query"));
		
	}
	
	@Test
	public void testTransform() throws IOException, ServletException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("collection", "cgi-transform");
		req.setParameter("query", "hello");
		req.setParameter("coverage", "abcnews");
		req.setParameter("scope", "/");
		
		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);
		
		Assert.assertTrue(chain.getRequest() instanceof RequestParametersTransformWrapper);
		Assert.assertEquals(6, chain.getRequest().getParameterMap().size());
		Assert.assertEquals("cgi-transform", chain.getRequest().getParameter("collection"));
		Assert.assertEquals("hello", chain.getRequest().getParameter("query"));
		Assert.assertEquals("abcnews", chain.getRequest().getParameter("coverage"));
		Assert.assertEquals("news", chain.getRequest().getParameter("profile"));
		Assert.assertEquals("/news", chain.getRequest().getParameter("scope"));
		Assert.assertArrayEquals(new String[] {"abc", "news"}, chain.getRequest().getParameterValues("clive"));
	}
	
	@Test
	public void testNoCollection() throws IOException, ServletException {
		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
		Assert.assertTrue(chain.getRequest() instanceof MockHttpServletRequest);
	}
	
	@Test
	public void testInvalidCollection() throws IOException, ServletException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("collection", "invalid");
		
		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(req, new MockHttpServletResponse(), chain);
		Assert.assertTrue(chain.getRequest() instanceof MockHttpServletRequest);
	}


	
}

