package com.funnelback.publicui.test.search.web.interceptors;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.search.web.interceptors.SearchLogInterceptor;
import com.funnelback.publicui.test.mock.MockLogService;

public class SearchLogInterceptorTests {

	private SearchLogInterceptor interceptor;
	private MockLogService logService;
	private SearchTransaction st;
	
	@Before
	public void before() throws FileNotFoundException, EnvironmentVariableException {
		logService = new MockLogService();
		interceptor = new SearchLogInterceptor();
		interceptor.setLogService(logService);
		
		st = new SearchTransaction(new SearchQuestion(), null);
		st.getQuestion().setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
		st.getQuestion().getCollection().getConfiguration().setValue(Keys.USERID_TO_LOG, DefaultValues.UserIdToLog.ip.toString());
		st.getQuestion().setCnClickedCluster("Clicked Cluster");
		st.getQuestion().getCnPreviousClusters().add("Previous Cluster");
		st.getQuestion().setUserId("1.2.3.4");
	}
	
	@Test
	public void testInterceptorPrehandleDoesntBlock() throws Exception {
		Assert.assertTrue(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null));
	}
	
	@Test
	public void testMissingParams() throws Exception {
		// No ModelAndView
		interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, null);
		Assert.assertEquals(0, logService.getCnLogs().size());
		
		// Empty ModelAndView
		interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, new ModelAndView());
		Assert.assertEquals(0, logService.getCnLogs().size());
		
		// Empty search transaction
		ModelAndView mav = new ModelAndView();
		mav.addObject(SearchController.MODEL_KEY_SEARCH_TRANSACTION, new SearchTransaction(null, null));
		interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, mav);
		Assert.assertEquals(0, logService.getCnLogs().size());
		
		// Empty question & response
		mav.addObject(SearchController.MODEL_KEY_SEARCH_TRANSACTION, new SearchTransaction(new SearchQuestion(), new SearchResponse()));
		interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, mav);
		Assert.assertEquals(0, logService.getCnLogs().size());
		
		// No Clicked Clusters
		((SearchTransaction) mav.getModel().get(SearchController.MODEL_KEY_SEARCH_TRANSACTION)).getQuestion().setCollection(new Collection("dummy", null));
		interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, mav);
		Assert.assertEquals(0, logService.getCnLogs().size());
	}
	
	@Test
	public void testContextualNavigationLog() throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.addObject(SearchController.MODEL_KEY_SEARCH_TRANSACTION, st);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("5.6.7.8");
		
		interceptor.postHandle(request, null, null, mav);
		
		Assert.assertEquals(1, logService.getCnLogs().size());
		ContextualNavigationLog cnLog = logService.getCnLogs().get(0);
		
		Assert.assertEquals("Clicked Cluster", cnLog.getCluster());
		Assert.assertEquals("dummy", cnLog.getCollection().getId());
		Assert.assertNotNull(cnLog.getDate());
		Assert.assertEquals("Previous Cluster", cnLog.getPreviousClusters().get(0));
		Assert.assertNull("", cnLog.getProfile());
		Assert.assertEquals("userId should be taken from the SearchQuestion, not the request", "1.2.3.4", cnLog.getUserId());
	}
	
}
