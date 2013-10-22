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
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.xml.padre.StaxStreamParser;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import org.apache.commons.io.FileUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class RecommenderControllerTest {
    private final String DEFAULT_SEED_ITEM = "http://example.com/";
    private final String DEFAULT_RECOMMENDATION = "http://example.com/careers/";
    private final String DEFAULT_RECOMMENDATION_TITLE = "Careers @ Example.com";
    private final String EXPLORE_SEED = "http://www.immi.gov.au/";
    private final String EXPLORE_RECOMMENDATION = "http://www.immi.gov.au/e_visa/";
    private final String EXPLORE_RECOMMENDATION_TITLE = "Department of Immigation e-Visa";
    private final String DEFAULT_COLLECTION_NAME = "recommender";
    private final String INVALID_COLLECTION_NAME = "invalid_collection";
    private final int MAX_RECOMMENDATIONS = 10;

    @Autowired
    private MockConfigRepository configRepository;

    @Autowired
    private RecommenderController recommenderController;

    private MockHttpServletRequest request;
    private ModelAndView modelAndView;
    private SearchUser user;

    /**
     * Generate and return a mock DocInfo object based on the given URL address and title.
     * @param address address of URL
     * @param title title of URL content
     * @return DocInfo object
     */
    private DocInfo getMockDocInfo(String address, String title) {
        DocInfo docInfo;

        Map<String, String> i4uData = new HashMap<>();
        i4uData.put("url", address);
        i4uData.put("title", title);
        i4uData.put("filetype", "html");
        i4uData.put("sumrytext", "Document summary text for: " + address);
        i4uData.put("date", "");
        i4uData.put("unfiltered_length", "1024");
        i4uData.put("words_indexed", "2048");
        i4uData.put("flags", "1234");
        i4uData.put("qiescore", "1.234");
        docInfo = new DocInfo(i4uData);

        return docInfo;
    }

    /**
     * Return a mock SearchQuestion for the given collection name.
     * @param collectionName name of collection
     * @return a mock SearchQuestion
     */
    private SearchQuestion getMockSearchQuestion(String collectionName) {
        configRepository.removeAllCollections();
        configRepository.addCollection(new Collection(collectionName, null));
        Collection collection = new Collection(collectionName, new NoOptionsConfig(collectionName));
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collection);

        return sq;
    }

    private void checkResponse(RecommendationResponse recommendationResponse, String seedItem,
            String collectionName, RecommendationResponse.Source source, int maxRecommendations,
            String recommendedItem, String recommendedItemTitle) {
        Assert.assertEquals(RecommendationResponse.Status.OK, recommendationResponse.getStatus());
        Assert.assertEquals(seedItem, recommendationResponse.getSeedItem());
        Assert.assertEquals(collectionName, recommendationResponse.getCollection());
        Assert.assertEquals(source, recommendationResponse.getSource());
        Assert.assertEquals(maxRecommendations, recommendationResponse.getMaxRecommendations());

        List<Recommendation> returnedRecommendations = recommendationResponse.getRecommendations();
        Assert.assertEquals(1, returnedRecommendations.size());
        Recommendation returnedRecommendation = returnedRecommendations.get(0);
        Assert.assertEquals(recommendedItem, returnedRecommendation.getItemID());
        Assert.assertEquals(recommendedItemTitle, returnedRecommendation.getTitle());
    }

    @Before
    public void before() throws Exception {
        request = new MockHttpServletRequest();
        request.setRequestURI("similarItems.json");

        Map<String, Object> model = new HashMap<>();
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex.xml")), false));

        model.put(SearchController.ModelAttributes.response.toString(), searchResponse);
        modelAndView = new ModelAndView("json", model);
        user = new SearchUser();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidCollection() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(INVALID_COLLECTION_NAME);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                "", MAX_RECOMMENDATIONS, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMissingCollectionParameter() throws Exception {
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM, "",
                MAX_RECOMMENDATIONS, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSeedItem() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, "", "", MAX_RECOMMENDATIONS, null);
    }

    @Test
    public void testSeedNotFound() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);

        // Create a mock DataAPI service but don't configure it to return content for the default seed item.
        DataAPI dataAPI = mock(DataAPI.class);
        recommenderController.setDataAPI(dataAPI);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                "", MAX_RECOMMENDATIONS, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");
        RecommendationResponse.Status status = recommendationResponse.getStatus();
        Assert.assertEquals(RecommendationResponse.Status.SEED_NOT_FOUND, status);
    }
    
	@Test
    public void testSeedFound() throws Exception  {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();

        List<String> indexURLs = new ArrayList<>();
        indexURLs.add(DEFAULT_RECOMMENDATION);

        ItemTuple itemTuple = new ItemTuple(DEFAULT_RECOMMENDATION, 5);
        List<ItemTuple> items; items = new ArrayList<>();
        items.add(itemTuple);

        DocInfo docInfo = getMockDocInfo(DEFAULT_RECOMMENDATION, DEFAULT_RECOMMENDATION_TITLE);
        List<Recommendation> recommendations = new ArrayList<>();
        Recommendation recommendation = new Recommendation(DEFAULT_RECOMMENDATION, -1, docInfo);
        recommendations.add(recommendation);

        Map<String, ItemTuple> confidenceMap = new HashMap<>();
        confidenceMap.put(DEFAULT_RECOMMENDATION, itemTuple);

        DataAPI dataAPI = mock(DataAPI.class);
        when(dataAPI.getDocInfo(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(docInfo);
        when(dataAPI.decorateURLRecommendations(indexURLs, confidenceMap, collectionConfig,
                MAX_RECOMMENDATIONS)).thenReturn(recommendations);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(items);
        recommenderController.setRecommenderDAO(recommenderDAO);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                "", MAX_RECOMMENDATIONS, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");

        checkResponse(recommendationResponse, DEFAULT_SEED_ITEM, DEFAULT_COLLECTION_NAME,
                RecommendationResponse.Source.CLICKS, MAX_RECOMMENDATIONS, DEFAULT_RECOMMENDATION,
                DEFAULT_RECOMMENDATION_TITLE);
    }

    @Test
    public void testExploreRecommendations() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        int maxRecommendations = 5;

        List<String> indexURLs = new ArrayList<>();
        indexURLs.add(EXPLORE_SEED);

        ItemTuple itemTuple = new ItemTuple(EXPLORE_SEED, 5);
        List<ItemTuple> items; items = new ArrayList<>();
        items.add(itemTuple);

        List<Recommendation> recommendations = new ArrayList<>();
        DocInfo recommendationDocInfo = getMockDocInfo(EXPLORE_RECOMMENDATION, EXPLORE_RECOMMENDATION_TITLE);
        Recommendation recommendation = new Recommendation(EXPLORE_RECOMMENDATION, -1, recommendationDocInfo);
        recommendations.add(recommendation);

        Map<String, ItemTuple> confidenceMap = new HashMap<>();
        confidenceMap.put(EXPLORE_SEED, itemTuple);
        RecommendationResponse simulatedResponse = new RecommendationResponse(RecommendationResponse.Status.OK,
                EXPLORE_SEED, DEFAULT_COLLECTION_NAME, "", maxRecommendations, collectionConfig.getCollectionName(),
                RecommendationResponse.Source.EXPLORE, -1, recommendations);

        DataAPI dataAPI = mock(DataAPI.class);
        DocInfo seedDocInfo = getMockDocInfo(EXPLORE_SEED, "Immigration");
        when(dataAPI.getDocInfo(EXPLORE_SEED, collectionConfig)).thenReturn(seedDocInfo);
        when(dataAPI.decorateURLRecommendations(any(List.class), any(Map.class),
                any(Config.class), anyInt())).thenReturn(recommendations);
        when(dataAPI.getResponseFromResults(any(String.class), any(List.class), any(Config.class),
                any(String.class), any(String.class), anyInt())).thenReturn(simulatedResponse);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(EXPLORE_SEED, collectionConfig)).thenReturn(null);
        recommenderController.setRecommenderDAO(recommenderDAO);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("application/json");

        SearchController searchController = mock(SearchController.class);
        when(searchController.search(any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(SearchQuestion.class), any(SearchUser.class))).thenReturn(modelAndView);
        recommenderController.setSearchController(searchController);

        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, EXPLORE_SEED,
                "", maxRecommendations, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");
        RecommendationResponse.Status status = recommendationResponse.getStatus();
        Assert.assertEquals(RecommendationResponse.Status.OK, status);

        checkResponse(recommendationResponse, EXPLORE_SEED, DEFAULT_COLLECTION_NAME,
                RecommendationResponse.Source.EXPLORE, maxRecommendations, EXPLORE_RECOMMENDATION,
                EXPLORE_RECOMMENDATION_TITLE);
    }
}