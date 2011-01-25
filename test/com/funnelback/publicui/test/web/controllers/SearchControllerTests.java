package com.funnelback.publicui.test.web.controllers;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.web.controllers.SearchController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:test_data/spring/applicationContext.xml")
public class SearchControllerTests {

	@Autowired
	private SearchController searchController;
	
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
	
}
