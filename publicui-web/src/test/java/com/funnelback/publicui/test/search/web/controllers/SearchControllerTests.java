package com.funnelback.publicui.test.search.web.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
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
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.profile.ProfileId;
import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.common.profile.ProfileView;
import com.funnelback.config.configtypes.service.DefaultServiceConfigReadOnly;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.data.file.server.FileServerConfigDataReadOnly;
import com.funnelback.config.data.service.InMemoryServiceConfig;
import com.funnelback.config.factory.ConfigFactory;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.test.mock.MockDataFetcher;
import com.funnelback.publicui.test.mock.MockInputProcessor;
import com.funnelback.publicui.test.mock.MockOutputProcessor;

import waffle.windows.auth.IWindowsAccount;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsImpersonationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SearchControllerTests {

    @Autowired
    private MockConfigRepository configRepository;
    
    @Autowired
    private SearchController searchController;

    @Resource(name="inputFlow")
    private List<InputProcessor> inputFlow;
    
    @Resource(name="dataFetchers")
    private List<DataFetcher> dataFetchers;

    @Resource(name="outputFlow")
    private List<OutputProcessor> outputFlow;
    
    @Resource(name="searchHome")
    private File searchHome;
    
    private MockHttpServletRequest request;

    @Before
    public void before() {
        request = new MockHttpServletRequest();
        request.setRequestURI("search.xml");
        
        ((MockInputProcessor)inputFlow.get(0)).setThrowError(false);
        ((MockDataFetcher) dataFetchers.get(0)).setThrowError(false);
        ((MockOutputProcessor)outputFlow.get(0)).setThrowError(false);
        
        ((MockInputProcessor)inputFlow.get(0)).setTraversed(false);
        ((MockDataFetcher) dataFetchers.get(0)).setTraversed(false);
        ((MockOutputProcessor) outputFlow.get(0)).setTraversed(false);

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNoCollectionShouldReturnAllCollections() {
        configRepository.removeAllCollections();
        configRepository.addCollection(new Collection("test1", null));
        configRepository.addCollection(new Collection("test2", null));
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = searchController.noCollection(response); 
        ModelAndViewAssert.assertModelAttributeAvailable(mav, SearchController.ModelAttributes.AllCollections.toString());
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        
        List<Collection> collections = (List<Collection>) mav.getModelMap().get(SearchController.ModelAttributes.AllCollections.toString());
        Assert.assertEquals(2, collections.size());
        Assert.assertTrue("test1".equals(collections.get(0).getId()) || "test1".equals(collections.get(1).getId()));
        Assert.assertTrue("test2".equals(collections.get(0).getId()) || "test2".equals(collections.get(1).getId()));
    }
    
    @Test
    public void testNoQueryShouldReturnSearchTransactionWithResponse() throws Exception {
        Collection col = collectionWithProfile("test", new NoOptionsConfig(searchHome, "dummy"));
        SearchQuestion q = new SearchQuestion();
        q.setCollection(col);
        ModelAndView mav = searchController.search(request, new MockHttpServletResponse(), q, null);
        ModelAndViewAssert.assertModelAttributeAvailable(mav, SearchController.ModelAttributes.SearchTransaction.toString());

        SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString());
        Assert.assertNotNull(st.getQuestion().getCollection());
        Assert.assertEquals(col, st.getQuestion().getCollection());
        Assert.assertNotNull(st.getResponse());
        Assert.assertNull(st.getResponse().getResultPacket());
    }
    
    @Test
    public void testEmptyQueryShouldReturnSearchTransaction() throws Exception {
        Collection col = collectionWithProfile("test-collection", new NoOptionsConfig(searchHome, "dummy"));
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(col);
        sq.setQuery("");
        
        ModelAndView mav = searchController.search(request, new MockHttpServletResponse(), sq, null);
        ModelAndViewAssert.assertModelAttributeAvailable(mav, SearchController.ModelAttributes.SearchTransaction.toString());

        SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString());
        Assert.assertNotNull(st.getQuestion().getCollection());
        Assert.assertNotNull(st.getResponse());
        Assert.assertNull(st.getResponse().getResultPacket());
    }

    @Test
    public void testInvalidCollectionShouldShowCollectionList() throws Exception {
        configRepository.removeAllCollections();
        configRepository.addCollection(new Collection("test1", new NoOptionsConfig(searchHome, "dummy")));
        configRepository.addCollection(new Collection("test2", new NoOptionsConfig(searchHome, "dummy")));

        SearchQuestion sq = new SearchQuestion();
        sq.setQuery("test");
        
        request.setRequestURI("search.html");
        MockHttpServletResponse response = new MockHttpServletResponse(); 
        ModelAndView mav = searchController.search(request, response, sq, null);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        
        List<Collection> collections = (List<Collection>) mav.getModel().get(SearchController.ModelAttributes.AllCollections.toString());
        Assert.assertNotNull(collections);
        Assert.assertEquals(2, collections.size());
        Assert.assertTrue("test1".equals(collections.get(0).getId()) || "test1".equals(collections.get(1).getId()));
        Assert.assertTrue("test2".equals(collections.get(0).getId()) || "test2".equals(collections.get(1).getId()));
    }
    
    @Test
    public void testInvalidCollectionShouldNotReturnListForNonHtml() throws Exception {
        SearchQuestion sq = new SearchQuestion();
        sq.setQuery("test");
        
        request.setRequestURI("search.xml");
        MockHttpServletResponse response = new MockHttpServletResponse(); 
        Assert.assertNull(searchController.search(request, response, sq, null));
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());

        request.setRequestURI("search.json");
        response = new MockHttpServletResponse(); 
        Assert.assertNull(searchController.search(request, response, sq, null));
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
    
    @Test
    public void testSearchShouldProcessFlow() throws Exception {
        ((MockInputProcessor)inputFlow.get(0)).setTraversed(false);
        ((MockDataFetcher) dataFetchers.get(0)).setTraversed(false);
        ((MockOutputProcessor) outputFlow.get(0)).setTraversed(false);

        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collectionWithProfile("test-collection", new NoOptionsConfig(searchHome, "dummy")));
        sq.setQuery("test-query");
        ModelAndView mav = searchController.search(request, new MockHttpServletResponse(), sq, null);
        
        SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString());
        Assert.assertNotNull(st);
        Assert.assertNotNull(st.getQuestion());
        Assert.assertNotNull(st.getResponse());
        Assert.assertNull("Error should be null but was " + st.getError(), st.getError());
        
        Assert.assertEquals("test-collection", st.getQuestion().getCollection().getId());
        Assert.assertEquals("test-query", st.getQuestion().getQuery());
        
        Assert.assertTrue(((MockInputProcessor)inputFlow.get(0)).isTraversed());
        Assert.assertTrue(((MockDataFetcher) dataFetchers.get(0)).isTraversed());
        Assert.assertTrue(((MockOutputProcessor) outputFlow.get(0)).isTraversed());
    }
    
    @Test
    public void testInputProcessorErrorShouldGenerateSearchError() throws Exception {
        ((MockInputProcessor)inputFlow.get(0)).setThrowError(true);
        ((MockDataFetcher) dataFetchers.get(0)).setThrowError(false);
        ((MockOutputProcessor)outputFlow.get(0)).setThrowError(false);

        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collectionWithProfile("test-collection", new NoOptionsConfig(searchHome, "dummy")));
        sq.setQuery("test-query");
        ModelAndView mav = searchController.search(request, new MockHttpServletResponse(), sq, null);

        SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString());
        Assert.assertNotNull(st);
        Assert.assertNotNull(st.getQuestion());
        Assert.assertNotNull(st.getResponse());
        Assert.assertNotNull(st.getError());
        
        Assert.assertEquals(SearchError.Reason.InputProcessorError, st.getError().getReason());
    }
    
    @Test
    public void testDataFetcherErrorShouldGenerateSearchError() throws Exception {
        ((MockInputProcessor)inputFlow.get(0)).setThrowError(false);
        ((MockDataFetcher) dataFetchers.get(0)).setThrowError(true);
        ((MockOutputProcessor)outputFlow.get(0)).setThrowError(false);

        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collectionWithProfile("test-collection", new NoOptionsConfig(searchHome, "dummy")));
        sq.setQuery("test-query");
        ModelAndView mav = searchController.search(request, new MockHttpServletResponse(), sq, null);

        SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString());
        Assert.assertNotNull(st);
        Assert.assertNotNull(st.getQuestion());
        Assert.assertNotNull(st.getResponse());
        Assert.assertNotNull(st.getError());
        
        Assert.assertEquals(SearchError.Reason.DataFetchError, st.getError().getReason());
    }
    
    @Test
    public void testOutputProcessorErrorShouldGenerateSearchError() throws Exception {
        ((MockInputProcessor)inputFlow.get(0)).setThrowError(false);
        ((MockDataFetcher) dataFetchers.get(0)).setThrowError(false);
        ((MockOutputProcessor)outputFlow.get(0)).setThrowError(true);

        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collectionWithProfile("test-collection", new NoOptionsConfig(searchHome, "dummy")));
        sq.setQuery("test-query");
        ModelAndView mav = searchController.search(request, new MockHttpServletResponse(), sq, null);

        SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString());
        Assert.assertNotNull(st);
        Assert.assertNotNull(st.getQuestion());
        Assert.assertNotNull(st.getResponse());
        Assert.assertNotNull(st.getError());
        
        Assert.assertEquals(SearchError.Reason.OutputProcessorError, st.getError().getReason());
    }
    
    private class MockWindowsIdentity implements IWindowsIdentity {

        @Override
        public String getSidString() {
            return "";
        }

        @Override
        public byte[] getSid() {
            return new byte[0];
        }

        @Override
        public String getFqn() {
            return "";
        }

        @Override
        public IWindowsAccount[] getGroups() {
            return new IWindowsAccount[0];
        }

        @Override
        public IWindowsImpersonationContext impersonate() {
            return null;
        }

        @Override
        public void dispose() {    }

        @Override
        public boolean isGuest() {
            return false;
        }
        
    }
    
    /**
     * Note that like many tests in this class the collection name is different to collection for the config and the profile config.
     * @param name
     * @param config
     * @return
     * @throws ProfileNotFoundException
     */
    private Collection collectionWithProfile(String name, Config config) throws ProfileNotFoundException {
        Collection collection = new Collection(name, config);
        Profile p = new Profile("_default");
        ServiceConfigReadOnly serviceConfigReadOnly = new ConfigFactory(searchHome).readOnly().serviceConfig(new CollectionId("dummy"), new ProfileId("_default"), ProfileView.live);
        p.setServiceConfig(serviceConfigReadOnly);
        collection.getProfiles().put("_default", p);
        return collection;
    }
    
}
