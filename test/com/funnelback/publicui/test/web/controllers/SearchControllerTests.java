package com.funnelback.publicui.test.web.controllers;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockDataFetcher;
import com.funnelback.publicui.test.mock.MockInputProcessor;
import com.funnelback.publicui.test.mock.MockOutputProcessor;
import com.funnelback.publicui.web.controllers.SearchController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:test_data/spring/applicationContext.xml")
public class SearchControllerTests {

	@Autowired
	private SearchController searchController;

	@Resource(name="inputFlow")
	private List<InputProcessor> inputFlow;
	
	@Resource(name="dataFetchers")
	private List<DataFetcher> dataFetchers;

	@Resource(name="outputFlow")
	private List<OutputProcessor> outputFlow;

	@SuppressWarnings("unchecked")
	@Test
	public void testNoCollectionShouldReturnAllCollections() {
		ModelAndView mav = searchController.noCollection(); 
		ModelAndViewAssert.assertModelAttributeAvailable(mav, SearchController.MODEL_KEY_COLLECTION_LIST);
		
		List<Collection> collections = (List<Collection>) mav.getModelMap().get(SearchController.MODEL_KEY_COLLECTION_LIST);
		Assert.assertEquals(2, collections.size());
		Assert.assertEquals("collection1", collections.get(0).getId());
		Assert.assertEquals("collection2", collections.get(1).getId());
	}
	
	@Test
	public void testNoQueryShouldReturnSearchTransaction() {
		Collection col = new Collection("test", null);
		ModelAndView mav = searchController.noQuery(col);
		ModelAndViewAssert.assertModelAttributeAvailable(mav, SearchController.MODEL_KEY_SEARCH_TRANSACTION);

		SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.MODEL_KEY_SEARCH_TRANSACTION);
		Assert.assertNotNull(st.getQuestion().getCollection());
		Assert.assertEquals(col, st.getQuestion().getCollection());
		Assert.assertNull(st.getResponse());
	}
	
	@Test
	public void testSearchShouldProcessFlow() {
		((MockInputProcessor)inputFlow.get(0)).setTraversed(false);
		((MockDataFetcher) dataFetchers.get(0)).setTraversed(false);
		((MockOutputProcessor) outputFlow.get(0)).setTraversed(false);

		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(new Collection("test-collection", null));
		sq.setQuery("test-query");
		ModelAndView mav = searchController.search(new MockHttpServletRequest(), sq);
		
		SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.MODEL_KEY_SEARCH_TRANSACTION);
		Assert.assertNotNull(st);
		Assert.assertNotNull(st.getQuestion());
		Assert.assertNotNull(st.getResponse());
		Assert.assertNull(st.getError());
		
		Assert.assertEquals("test-collection", st.getQuestion().getCollection().getId());
		Assert.assertEquals("test-query", st.getQuestion().getQuery());
		
		Assert.assertTrue(((MockInputProcessor)inputFlow.get(0)).isTraversed());
		Assert.assertTrue(((MockDataFetcher) dataFetchers.get(0)).isTraversed());
		Assert.assertTrue(((MockOutputProcessor) outputFlow.get(0)).isTraversed());
	}
	
	@Test
	public void testInputProcessorErrorShouldGenerateSearchError() {
		((MockInputProcessor)inputFlow.get(0)).setThrowError(true);
		((MockDataFetcher) dataFetchers.get(0)).setThrowError(false);
		((MockOutputProcessor)outputFlow.get(0)).setThrowError(false);

		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(new Collection("test-collection", null));
		sq.setQuery("test-query");
		ModelAndView mav = searchController.search(new MockHttpServletRequest(), sq);

		SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.MODEL_KEY_SEARCH_TRANSACTION);
		Assert.assertNotNull(st);
		Assert.assertNotNull(st.getQuestion());
		Assert.assertNotNull(st.getResponse());
		Assert.assertNotNull(st.getError());
		
		Assert.assertEquals(SearchError.Reason.InputProcessorError, st.getError().getReason());
	}
	
	@Test
	public void testDataFetcherErrorShouldGenerateSearchError() {
		((MockInputProcessor)inputFlow.get(0)).setThrowError(false);
		((MockDataFetcher) dataFetchers.get(0)).setThrowError(true);
		((MockOutputProcessor)outputFlow.get(0)).setThrowError(false);

		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(new Collection("test-collection", null));
		sq.setQuery("test-query");
		ModelAndView mav = searchController.search(new MockHttpServletRequest(), sq);

		SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.MODEL_KEY_SEARCH_TRANSACTION);
		Assert.assertNotNull(st);
		Assert.assertNotNull(st.getQuestion());
		Assert.assertNotNull(st.getResponse());
		Assert.assertNotNull(st.getError());
		
		Assert.assertEquals(SearchError.Reason.DataFetchError, st.getError().getReason());
	}
	
	@Test
	public void testOutputProcessorErrorShouldGenerateSearchError() {
		((MockInputProcessor)inputFlow.get(0)).setThrowError(false);
		((MockDataFetcher) dataFetchers.get(0)).setThrowError(false);
		((MockOutputProcessor)outputFlow.get(0)).setThrowError(true);

		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(new Collection("test-collection", null));
		sq.setQuery("test-query");
		ModelAndView mav = searchController.search(new MockHttpServletRequest(), sq);

		SearchTransaction st = (SearchTransaction) mav.getModel().get(SearchController.MODEL_KEY_SEARCH_TRANSACTION);
		Assert.assertNotNull(st);
		Assert.assertNotNull(st.getQuestion());
		Assert.assertNotNull(st.getResponse());
		Assert.assertNotNull(st.getError());
		
		Assert.assertEquals(SearchError.Reason.OutputProcessorError, st.getError().getReason());
	}

	
}
