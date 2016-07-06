package com.funnelback.publicui.test.search.web.interceptors;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.FacetedNavigationLog;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
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
        
        st.setSession(new SearchSession(new SearchUser("user-id")));
        
        st.getQuestion().setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        st.getQuestion().getCollection().getConfiguration().setValue(Keys.REQUEST_ID_TO_LOG, DefaultValues.RequestId.ip.toString());
        st.getQuestion().setCnClickedCluster("Clicked Cluster");
        st.getQuestion().getCnPreviousClusters().add("Previous Cluster");
        st.getQuestion().setRequestId("1.2.3.4");
        st.getQuestion().setOriginalQuery("query terms");
        
        st.getQuestion().getSelectedCategoryValues().put("f.Type|Y", Arrays.asList(new String[] {"Part-time", "Full-time"}));
        st.getQuestion().getSelectedCategoryValues().put("f.Location|X", Arrays.asList(new String[] {"Sydney", "Canberra"}));
        st.getQuestion().getSelectedFacets().add("Type");
        st.getQuestion().getSelectedFacets().add("Location");
        
        CategoryDefinition cDef = new MetadataFieldFill("X");
        cDef.setFacetName("Location");
        cDef.setLabel("X");
        FacetDefinition fDef = new FacetDefinition("Location", Arrays.asList(new CategoryDefinition[] {cDef}));

        CategoryDefinition cDef2 = new MetadataFieldFill("Y");
        cDef2.setFacetName("Type");
        cDef2.setLabel("Y");
        FacetDefinition fDef2 = new FacetDefinition("Type", Arrays.asList(new CategoryDefinition[] {cDef2}));

        FacetedNavigationConfig fnConf = new FacetedNavigationConfig("", Arrays.asList(new FacetDefinition[] {fDef, fDef2}));
        st.getQuestion().getCollection().setFacetedNavigationLiveConfig(fnConf);
        
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
        Assert.assertEquals(0, logService.getFnLogs().size());
        
        // Empty ModelAndView
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, new ModelAndView());
        Assert.assertEquals(0, logService.getCnLogs().size());
        Assert.assertEquals(0, logService.getFnLogs().size());
        
        // Empty search transaction
        ModelAndView mav = new ModelAndView();
        mav.addObject(SearchController.ModelAttributes.SearchTransaction.toString(), new SearchTransaction(null, null));
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, mav);
        Assert.assertEquals(0, logService.getCnLogs().size());
        Assert.assertEquals(0, logService.getFnLogs().size());
        
        // Empty question & response
        mav.addObject(SearchController.ModelAttributes.SearchTransaction.toString(), new SearchTransaction(new SearchQuestion(), new SearchResponse()));
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, mav);
        Assert.assertEquals(0, logService.getCnLogs().size());
        Assert.assertEquals(0, logService.getFnLogs().size());
        
        // No Clicked Clusters or selected facets
        ((SearchTransaction) mav.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString())).getQuestion().setCollection(new Collection("dummy", null));
        interceptor.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null, mav);
        Assert.assertEquals(0, logService.getCnLogs().size());
        Assert.assertEquals(0, logService.getFnLogs().size());
    }
    
    @Test
    public void testContextualNavigationLog() throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.addObject(SearchController.ModelAttributes.SearchTransaction.toString(), st);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("5.6.7.8");
        
        interceptor.postHandle(request, null, null, mav);
        
        Assert.assertEquals(1, logService.getCnLogs().size());
        ContextualNavigationLog cnLog = logService.getCnLogs().get(0);
        
        Assert.assertEquals("Clicked Cluster", cnLog.getCluster());
        Assert.assertEquals("dummy", cnLog.getCollection().getId());
        Assert.assertNotNull(cnLog.getDate());
        Assert.assertEquals("Previous Cluster", cnLog.getPreviousClusters().get(0));
        Assert.assertNull(cnLog.getProfile());
        Assert.assertEquals("requestId should be taken from the SearchQuestion, not the request", "1.2.3.4", cnLog.getRequestId());
        Assert.assertEquals("user-id", cnLog.getUserId());
    }
    
    @Test
    public void testSelectedFacetsButNoCategories() throws Exception {
        st.getQuestion().getSelectedCategoryValues().clear();
        ModelAndView mav = new ModelAndView();
        mav.addObject(SearchController.ModelAttributes.SearchTransaction.toString(), st);
        
        interceptor.postHandle(new MockHttpServletRequest(), null, null, mav);
        Assert.assertEquals(0, logService.getFnLogs().size());
    }

    @Test
    public void testSelectedCategoriesButNoFacets() throws Exception {
        st.getQuestion().getSelectedFacets().clear();
        ModelAndView mav = new ModelAndView();
        mav.addObject(SearchController.ModelAttributes.SearchTransaction.toString(), st);
        
        interceptor.postHandle(new MockHttpServletRequest(), null, null, mav);
        Assert.assertEquals(0, logService.getFnLogs().size());
    }

    @Test
    public void testInvalidFacet() throws Exception {
        st.getQuestion().getSelectedFacets().clear();
        st.getQuestion().getSelectedFacets().add("Invalid");
        
        st.getQuestion().getSelectedCategoryValues().clear();
        st.getQuestion().getSelectedCategoryValues().put("f.Invalid|X", Arrays.asList(new String[] {"a", "b"}));
        
        ModelAndView mav = new ModelAndView();
        mav.addObject(SearchController.ModelAttributes.SearchTransaction.toString(), st);
        
        interceptor.postHandle(new MockHttpServletRequest(), null, null, mav);
        Assert.assertEquals(0, logService.getFnLogs().size());
    }

    @Test
    public void testFacetedNavigationLog() throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.addObject(SearchController.ModelAttributes.SearchTransaction.toString(), st);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("5.6.7.8");
        
        interceptor.postHandle(request, null, null, mav);
        Assert.assertEquals(1, logService.getFnLogs().size());
        FacetedNavigationLog fnLog = logService.getFnLogs().get(0);
        
        Assert.assertEquals("Location: Canberra + Sydney, Type: Full-time + Part-time", fnLog.getFacet());
        Assert.assertEquals("query terms", fnLog.getQuery());
        Assert.assertEquals("dummy", fnLog.getCollection().getId());
        Assert.assertNotNull(fnLog.getDate());
        Assert.assertNull(fnLog.getProfile());
        Assert.assertEquals("requestId should be taken from the SearchQuestion, not the request", "1.2.3.4", fnLog.getRequestId());
        Assert.assertEquals("user-id", fnLog.getUserId());
        
    }
    
}
