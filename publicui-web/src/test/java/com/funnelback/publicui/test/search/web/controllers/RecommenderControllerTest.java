package com.funnelback.publicui.test.search.web.controllers;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.RecommendationResponse;
import com.funnelback.publicui.recommender.dao.RecommenderDAO;
import com.funnelback.publicui.recommender.dataapi.DataAPI;
import com.funnelback.publicui.recommender.web.controllers.RecommenderController;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class RecommenderControllerTest {
    private final String DEFAULT_SEED_ITEM = "http://example.com/";
    private final String DEFAULT_COLLECTION_NAME = "recommender";
    private final String INVALID_COLLECTION_NAME = "invalid_collection";
    private final int MAX_RECOMMENDATIONS = 10;

    @Autowired
    private MockConfigRepository configRepository;

    @Autowired
    private RecommenderController recommenderController;

    private MockHttpServletRequest request;
    private DocInfo docInfo;
    private List<ItemTuple> items;
    private List<String> indexURLs;
    private List<Recommendation> recommendations;
    private  Map<String, ItemTuple> confidenceMap;
    
    @Before
    public void before() {
        request = new MockHttpServletRequest();
        request.setRequestURI("similarItems.json");
        Map<String, String> i4uData = new HashMap<>();
        i4uData.put("url", DEFAULT_SEED_ITEM);
        i4uData.put("title", "Document Title");
        i4uData.put("filetype", "html");
        i4uData.put("sumrytext", "Document summary text");
        i4uData.put("date", "");
        i4uData.put("unfiltered_length", "1024");
        i4uData.put("words_indexed", "2048");
        i4uData.put("flags", "1234");
        i4uData.put("qiescore", "1.234");
        docInfo = new DocInfo(i4uData);

        ItemTuple itemTuple = new ItemTuple(DEFAULT_SEED_ITEM, 5);
        items = new ArrayList<>();
        items.add(itemTuple);

        indexURLs = new ArrayList<>();
        indexURLs.add(DEFAULT_SEED_ITEM);

        recommendations = new ArrayList<>();
        Recommendation recommendation = new Recommendation(DEFAULT_SEED_ITEM, -1, docInfo);
        recommendations.add(recommendation);

        confidenceMap = new HashMap<>();
        confidenceMap.put(DEFAULT_SEED_ITEM, itemTuple);

    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidCollection() throws Exception {
        configRepository.removeAllCollections();
        configRepository.addCollection(new Collection(INVALID_COLLECTION_NAME, null));
        Collection collection = new Collection(INVALID_COLLECTION_NAME, new NoOptionsConfig(INVALID_COLLECTION_NAME));
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collection);

        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM, "", MAX_RECOMMENDATIONS, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMissingCollectionParameter() throws Exception {
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(null);

        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM, "", MAX_RECOMMENDATIONS, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSeedItem() throws Exception {
        configRepository.removeAllCollections();
        configRepository.addCollection(new Collection(DEFAULT_COLLECTION_NAME, null));
        Collection collection = new Collection(DEFAULT_COLLECTION_NAME, new NoOptionsConfig(DEFAULT_COLLECTION_NAME));
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collection);

        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, "", "", MAX_RECOMMENDATIONS, null);
    }

    @Test
    public void testSeedNotFound() throws Exception {
        configRepository.removeAllCollections();
        configRepository.addCollection(new Collection(DEFAULT_COLLECTION_NAME, null));
        Collection collection = new Collection(DEFAULT_COLLECTION_NAME, new NoOptionsConfig(DEFAULT_COLLECTION_NAME));
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collection);

        // Create a mock DataAPI service but don't configure it to return content for the default seed item.
        DataAPI dataAPI = mock(DataAPI.class);
        recommenderController.setDataAPI(dataAPI);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM, "", MAX_RECOMMENDATIONS, null);
        RecommendationResponse recommendationResponse = (RecommendationResponse) mav.getModel().get("RecommendationResponse");
        RecommendationResponse.Status status = recommendationResponse.getStatus();
        Assert.assertEquals(RecommendationResponse.Status.SEED_NOT_FOUND, status);
    }
    
	@Test
    public void testSeedFound() throws Exception  {
        configRepository.removeAllCollections();
        configRepository.addCollection(new Collection(DEFAULT_COLLECTION_NAME, null));
        Collection collection = new Collection(DEFAULT_COLLECTION_NAME, new NoOptionsConfig(DEFAULT_COLLECTION_NAME));
        Config collectionConfig = collection.getConfiguration();
        
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collection);
        
        DataAPI dataAPI = mock(DataAPI.class);
        when(dataAPI.getDocInfo(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(docInfo);
        when(dataAPI.decorateURLRecommendations(indexURLs, confidenceMap, collectionConfig, MAX_RECOMMENDATIONS)).thenReturn(recommendations);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(items);
        recommenderController.setRecommenderDAO(recommenderDAO);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM, "", MAX_RECOMMENDATIONS, null);
        RecommendationResponse recommendationResponse = (RecommendationResponse) mav.getModel().get("RecommendationResponse");
        RecommendationResponse.Status status = recommendationResponse.getStatus();
        Assert.assertEquals(RecommendationResponse.Status.OK, status);
    }
}
