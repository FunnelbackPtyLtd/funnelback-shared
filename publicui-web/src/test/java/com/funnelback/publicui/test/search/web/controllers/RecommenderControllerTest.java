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
    private final String DEFAULT_SEED_ITEM_TITLE = "Example.com - Home";
    private final String DEFAULT_RECOMMENDATION = "http://example.com/careers/";
    private final String DEFAULT_RECOMMENDATION_TITLE = "Careers @ Example.com";
    private final String EXPLORE_SEED = "http://www.immi.gov.au/";
    private final String EXPLORE_RECOMMENDATION = "http://www.immi.gov.au/e_visa/";
    private final String EXPLORE_RECOMMENDATION_TITLE = "Department of Immigation e-Visa";
    private final String OUTSIDE_SCOPE = "http://example.com/hr/";
    private final String OUTSIDE_SCOPE_TITLE = "HR @ Example.com - Internal Only";
    private final String DEFAULT_COLLECTION_NAME = "recommender";
    private final String INVALID_COLLECTION_NAME = "invalid_collection";
    private final int MAX_RECOMMENDATIONS = 10;
    private final int DEFAULT_SCORE = 5;
    private final int DEFAULT_CONFIDENCE = -1;

    @Autowired
    private MockConfigRepository configRepository;

    @Autowired
    private RecommenderController recommenderController;

    private MockHttpServletRequest request;
    private ModelAndView modelAndView;
    private String scope;
    private List<String> indexURLs;
    private List<String> indexURLTitles;

    /**
     * Generate and return a mock DocInfo object based on the given URL address and title.
     *
     * @param address address of URL
     * @param title   title of URL content
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
     *
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
                               String recommendedItem, String recommendedItemTitle, int expectedSize,
                               RecommendationResponse.Status status) {
        Assert.assertEquals(status, recommendationResponse.getStatus());
        Assert.assertEquals(seedItem, recommendationResponse.getSeedItem());
        Assert.assertEquals(collectionName, recommendationResponse.getCollection());
        Assert.assertEquals(source, recommendationResponse.getSource());
        Assert.assertEquals(maxRecommendations, recommendationResponse.getMaxRecommendations());

        List<Recommendation> returnedRecommendations = recommendationResponse.getRecommendations();

        int actualSize = returnedRecommendations.size();
        Assert.assertEquals(expectedSize, actualSize);

        if (expectedSize > 0) {
            Recommendation returnedRecommendation = returnedRecommendations.get(0);
            Assert.assertEquals(recommendedItem, returnedRecommendation.getItemID());
            Assert.assertEquals(recommendedItemTitle, returnedRecommendation.getTitle());
        }
    }

    private List<ItemTuple> getItemTuples(List<String> items, List<String> titles) {
        List<ItemTuple> itemTuples = new ArrayList<>();

        for (int i=0; i < items.size(); i++) {
            ItemTuple itemTuple = new ItemTuple(items.get(i), DEFAULT_SCORE, titles.get(i));
            itemTuples.add(itemTuple);
        }

        return itemTuples;
    }

    private List<Recommendation> getRecommendations(List<ItemTuple> itemTuples) {
        List<Recommendation> recommendations = new ArrayList<>();

        for (ItemTuple itemTuple : itemTuples) {
            DocInfo docInfo = getMockDocInfo(itemTuple.getItemID(), itemTuple.getTitle());
            Recommendation recommendation = new Recommendation(itemTuple.getItemID(), DEFAULT_CONFIDENCE, docInfo);
            recommendations.add(recommendation);
        }

        return recommendations;
    }

    private void runSourceRequest(SearchQuestion sq, String sourceType, int numExpected,
                                  RecommendationResponse.Status status) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                scope, MAX_RECOMMENDATIONS, sourceType);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");

        if (RecommendationResponse.Source.valueOf(sourceType).equals(RecommendationResponse.Source.DEFAULT)) {
            // Assume we will get clicks back
            sourceType = RecommendationResponse.Source.CLICKS.toString();
        }

        checkResponse(recommendationResponse, DEFAULT_SEED_ITEM, DEFAULT_COLLECTION_NAME,
                RecommendationResponse.Source.valueOf(sourceType), MAX_RECOMMENDATIONS, DEFAULT_RECOMMENDATION,
                DEFAULT_RECOMMENDATION_TITLE, numExpected, status);
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
        scope = "";

        indexURLs = new ArrayList<>();
        indexURLs.add(DEFAULT_RECOMMENDATION);

        indexURLTitles = new ArrayList<>();
        indexURLTitles.add(DEFAULT_RECOMMENDATION_TITLE);

        SearchController searchController = mock(SearchController.class);
        when(searchController.search(any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(SearchQuestion.class), any(SearchUser.class))).thenReturn(modelAndView);
        recommenderController.setSearchController(searchController);
    }

    /****************************************** Start Tests *******************************************/

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCollection() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(INVALID_COLLECTION_NAME);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                scope, MAX_RECOMMENDATIONS, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingCollectionParameter() throws Exception {
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM, scope,
                MAX_RECOMMENDATIONS, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSeedItem() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, "", scope, MAX_RECOMMENDATIONS, null);
    }

    @Test
    public void testSeedNotFound() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);

        // Create a mock DataAPI service but don't configure it to return content for the default seed item.
        DataAPI dataAPI = mock(DataAPI.class);
        recommenderController.setDataAPI(dataAPI);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                scope, MAX_RECOMMENDATIONS, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");
        RecommendationResponse.Status status = recommendationResponse.getStatus();
        Assert.assertEquals(RecommendationResponse.Status.SEED_NOT_FOUND, status);
    }

    @Test
    public void testSeedFound() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();

        List<ItemTuple> cachedItems = getItemTuples(indexURLs, indexURLTitles);
        List<Recommendation> recommendations = getRecommendations(cachedItems);
        Map<String, ItemTuple> confidenceMap = new HashMap<>();
        confidenceMap.put(DEFAULT_RECOMMENDATION, cachedItems.get(0));

        DataAPI dataAPI = mock(DataAPI.class);
        DocInfo seedDocInfo = getMockDocInfo(DEFAULT_SEED_ITEM, DEFAULT_SEED_ITEM_TITLE);
        when(dataAPI.getDocInfo(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(seedDocInfo);
        when(dataAPI.decorateURLRecommendations(indexURLs, confidenceMap, collectionConfig
        )).thenReturn(recommendations);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(cachedItems);
        recommenderController.setRecommenderDAO(recommenderDAO);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                scope, MAX_RECOMMENDATIONS, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");

        checkResponse(recommendationResponse, DEFAULT_SEED_ITEM, DEFAULT_COLLECTION_NAME,
                RecommendationResponse.Source.CLICKS, MAX_RECOMMENDATIONS, DEFAULT_RECOMMENDATION,
                DEFAULT_RECOMMENDATION_TITLE, 1, RecommendationResponse.Status.OK);
    }

    @Test
    public void testExploreRecommendations() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        int maxRecommendations = 5;

        List<String> indexURLs = new ArrayList<>();
        indexURLs.add(EXPLORE_RECOMMENDATION);
        List<String> indexURLTitles = new ArrayList<>();
        indexURLTitles.add(EXPLORE_RECOMMENDATION_TITLE);

        List<ItemTuple> cachedItems = getItemTuples(indexURLs, indexURLTitles);
        List<Recommendation> recommendations = getRecommendations(cachedItems);

        RecommendationResponse simulatedResponse = new RecommendationResponse(RecommendationResponse.Status.OK,
                EXPLORE_SEED, DEFAULT_COLLECTION_NAME, scope, maxRecommendations, collectionConfig.getCollectionName(),
                RecommendationResponse.Source.EXPLORE, -1, recommendations);

        DataAPI dataAPI = mock(DataAPI.class);
        DocInfo seedDocInfo = getMockDocInfo(EXPLORE_SEED, "Immigration");
        when(dataAPI.getDocInfo(EXPLORE_SEED, collectionConfig)).thenReturn(seedDocInfo);
        when(dataAPI.decorateURLRecommendations(any(List.class), any(Map.class),
                any(Config.class))).thenReturn(recommendations);
        when(dataAPI.getResponseFromResults(any(String.class), any(List.class), any(Config.class),
                any(String.class), any(String.class), anyInt())).thenReturn(simulatedResponse);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(EXPLORE_SEED, collectionConfig)).thenReturn(null);
        recommenderController.setRecommenderDAO(recommenderDAO);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("application/json");

        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, EXPLORE_SEED,
                scope, maxRecommendations, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");
        RecommendationResponse.Status status = recommendationResponse.getStatus();
        Assert.assertEquals(RecommendationResponse.Status.OK, status);

        checkResponse(recommendationResponse, EXPLORE_SEED, DEFAULT_COLLECTION_NAME,
                RecommendationResponse.Source.EXPLORE, maxRecommendations, EXPLORE_RECOMMENDATION,
                EXPLORE_RECOMMENDATION_TITLE, 1, RecommendationResponse.Status.OK);
    }

    @Test
    public void testPositiveScope() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        scope = "careers";

        // Include an item that is out of scope in the list of cached items, and generate a "scoped items"
        // list that does not include this so a synthetic list of scoped recommendations can be generated.
        indexURLs.add(OUTSIDE_SCOPE);
        indexURLTitles.add(OUTSIDE_SCOPE_TITLE);

        List<ItemTuple> cachedItems = getItemTuples(indexURLs, indexURLTitles);
        List<ItemTuple> scopedItems = cachedItems;
        scopedItems.remove(1);
        indexURLs.remove(1);

        List<Recommendation> recommendations = getRecommendations(scopedItems);

        Map<String, ItemTuple> confidenceMap = new HashMap<>();
        confidenceMap.put(DEFAULT_RECOMMENDATION, scopedItems.get(0));

        DataAPI dataAPI = mock(DataAPI.class);
        DocInfo seedDocInfo = getMockDocInfo(DEFAULT_SEED_ITEM, DEFAULT_SEED_ITEM_TITLE);
        when(dataAPI.getDocInfo(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(seedDocInfo);
        when(dataAPI.decorateURLRecommendations(indexURLs, confidenceMap, collectionConfig
        )).thenReturn(recommendations);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(cachedItems);
        recommenderController.setRecommenderDAO(recommenderDAO);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                scope, MAX_RECOMMENDATIONS, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");

        checkResponse(recommendationResponse, DEFAULT_SEED_ITEM, DEFAULT_COLLECTION_NAME,
                RecommendationResponse.Source.CLICKS, MAX_RECOMMENDATIONS, DEFAULT_RECOMMENDATION,
                DEFAULT_RECOMMENDATION_TITLE, 1, RecommendationResponse.Status.OK);
    }

    @Test
    public void testNegativeScope() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        scope = "-hr";

        // Include an item that is out of scope in the list of cached items, and generate a "scoped items"
        // list that does not include this so a synthetic list of scoped recommendations can be generated.
        indexURLs.add(OUTSIDE_SCOPE);
        indexURLTitles.add(OUTSIDE_SCOPE_TITLE);

        List<ItemTuple> cachedItems = getItemTuples(indexURLs, indexURLTitles);
        List<ItemTuple> scopedItems = new ArrayList(cachedItems);
        scopedItems.remove(1);
        indexURLs.remove(1);

        List<Recommendation> recommendations = getRecommendations(scopedItems);

        Map<String, ItemTuple> confidenceMap = new HashMap<>();
        confidenceMap.put(DEFAULT_RECOMMENDATION, scopedItems.get(0));

        DataAPI dataAPI = mock(DataAPI.class);
        DocInfo seedDocInfo = getMockDocInfo(DEFAULT_SEED_ITEM, DEFAULT_SEED_ITEM_TITLE);
        when(dataAPI.getDocInfo(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(seedDocInfo);
        when(dataAPI.decorateURLRecommendations(indexURLs, confidenceMap, collectionConfig
        )).thenReturn(recommendations);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(cachedItems);
        recommenderController.setRecommenderDAO(recommenderDAO);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                scope, MAX_RECOMMENDATIONS, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");

        checkResponse(recommendationResponse, DEFAULT_SEED_ITEM, DEFAULT_COLLECTION_NAME,
                RecommendationResponse.Source.CLICKS, MAX_RECOMMENDATIONS, DEFAULT_RECOMMENDATION,
                DEFAULT_RECOMMENDATION_TITLE, 1, RecommendationResponse.Status.OK);
    }

    @Test
    public void testMaxRecommendations() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        int maxRecommendations = 1;

        indexURLs.add(OUTSIDE_SCOPE);
        indexURLTitles.add(OUTSIDE_SCOPE_TITLE);

        List<ItemTuple> cachedItems = getItemTuples(indexURLs, indexURLTitles);
        List<Recommendation> recommendations = getRecommendations(cachedItems);

        Map<String, ItemTuple> confidenceMap = new HashMap<>();
        confidenceMap.put(DEFAULT_RECOMMENDATION, cachedItems.get(0));        
        confidenceMap.put(OUTSIDE_SCOPE, cachedItems.get(1));

        DataAPI dataAPI = mock(DataAPI.class);
        DocInfo seedDocInfo = getMockDocInfo(DEFAULT_SEED_ITEM, DEFAULT_SEED_ITEM_TITLE);
        when(dataAPI.getDocInfo(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(seedDocInfo);
        when(dataAPI.decorateURLRecommendations(indexURLs, confidenceMap, collectionConfig
        )).thenReturn(recommendations);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(cachedItems);
        recommenderController.setRecommenderDAO(recommenderDAO);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                scope, maxRecommendations, null);
        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");

        checkResponse(recommendationResponse, DEFAULT_SEED_ITEM, DEFAULT_COLLECTION_NAME,
                RecommendationResponse.Source.CLICKS, maxRecommendations, DEFAULT_RECOMMENDATION,
                DEFAULT_RECOMMENDATION_TITLE, 1, RecommendationResponse.Status.OK);
    }

    /**
     * Test sourceType (Default, Clicks, Explore, None).
     * @throws Exception
     */
    @Test
    public void testSource() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();

        indexURLs.add(OUTSIDE_SCOPE);
        indexURLTitles.add(OUTSIDE_SCOPE_TITLE);

        List<ItemTuple> cachedItems = getItemTuples(indexURLs, indexURLTitles);

        List<Recommendation> recommendations = getRecommendations(cachedItems);

        Map<String, ItemTuple> confidenceMap = new HashMap<>();
        confidenceMap.put(DEFAULT_RECOMMENDATION, cachedItems.get(0));
        confidenceMap.put(OUTSIDE_SCOPE, cachedItems.get(1));

        DataAPI dataAPI = mock(DataAPI.class);
        DocInfo seedDocInfo = getMockDocInfo(DEFAULT_SEED_ITEM, DEFAULT_SEED_ITEM_TITLE);
        when(dataAPI.getDocInfo(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(seedDocInfo);
        when(dataAPI.decorateURLRecommendations(any(List.class), any(Map.class),
                any(Config.class))).thenReturn(recommendations);
        recommenderController.setDataAPI(dataAPI);

        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(cachedItems);
        recommenderController.setRecommenderDAO(recommenderDAO);

        runSourceRequest(sq, RecommendationResponse.Source.DEFAULT.toString(), 2, RecommendationResponse.Status.OK);
        runSourceRequest(sq, RecommendationResponse.Source.CLICKS.toString(), 2, RecommendationResponse.Status.OK);
        runSourceRequest(sq, RecommendationResponse.Source.NONE.toString(), 0, RecommendationResponse.Status.NO_SUGGESTIONS_FOUND);

        // Set things up for an Explore source by now returning a smaller list in response to an
        // explicit Explore request
        recommendations.remove(1);
        RecommendationResponse simulatedResponse = new RecommendationResponse(RecommendationResponse.Status.OK,
                DEFAULT_SEED_ITEM, DEFAULT_COLLECTION_NAME, scope, MAX_RECOMMENDATIONS, collectionConfig.getCollectionName(),
                RecommendationResponse.Source.EXPLORE, -1, recommendations);
        when(dataAPI.getResponseFromResults(any(String.class), any(List.class), any(Config.class),
                any(String.class), any(String.class), anyInt())).thenReturn(simulatedResponse);

        runSourceRequest(sq, RecommendationResponse.Source.EXPLORE.toString(), 1, RecommendationResponse.Status.OK);

        // Now configure DAO to return nothing for the given seed item i.e. no clicked recommendations available
        // This is so we can confirm that it does not fall through to returning Explore results (default behaviour).
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(null);
        runSourceRequest(sq, RecommendationResponse.Source.CLICKS.toString(), 0,
                RecommendationResponse.Status.NO_SUGGESTIONS_FOUND);
    }
}