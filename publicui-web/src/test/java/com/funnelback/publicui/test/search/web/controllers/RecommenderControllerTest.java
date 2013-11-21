package com.funnelback.publicui.test.search.web.controllers;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
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
    private final String META_COLLECTION_NAME = "recommender-meta";
    private final int MAX_RECOMMENDATIONS = 5;
    private final int DEFAULT_SCORE = 5;
    private final int DEFAULT_CONFIDENCE = -1;

    @Autowired
    private MockConfigRepository configRepository;

    @Autowired
    private RecommenderController recommenderController;

    private MockHttpServletRequest request;
    private ModelAndView modelAndView;
    private List<String> indexURLs;
    private List<String> indexURLTitles;

    /**
     * Check a "similarItems" request based on the given parameters.
     */
    private void checkSimilarItems(SearchQuestion sq, String expectedSourceType, int numExpected,
                                   RecommendationResponse.Status expectedStatus, int maxRecommendations,
                                   String expectedScope, String collectionName) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav;

        // If default constant is specified use this as an opportunity to see if controller
        // converts null to the expected default
        if (maxRecommendations == MAX_RECOMMENDATIONS) {
            mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                    expectedScope, null, expectedSourceType);
        }
        else {
            mav = recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM,
                    expectedScope, maxRecommendations, expectedSourceType);
        }

        RecommendationResponse recommendationResponse =
                (RecommendationResponse) mav.getModel().get("RecommendationResponse");

        if (expectedSourceType == null ||
                ItemTuple.Source.valueOf(expectedSourceType).equals(ItemTuple.Source.DEFAULT)) {
            // Assume we will get clicks back
            expectedSourceType = ItemTuple.Source.CLICKS.toString();
        }

        checkResponse(recommendationResponse, DEFAULT_SEED_ITEM, collectionName,
                ItemTuple.Source.valueOf(expectedSourceType), maxRecommendations, DEFAULT_RECOMMENDATION,
                DEFAULT_RECOMMENDATION_TITLE, numExpected, expectedStatus);
    }

    /**
     * Check the given response against the other specified values.
     */
    private void checkResponse(RecommendationResponse recommendationResponse, String seedItem,
                               String collectionName, ItemTuple.Source source, int maxRecommendations,
                               String recommendedItem, String recommendedItemTitle, int expectedSize,
                               RecommendationResponse.Status status) {
        int actualSize = 0;

        Assert.assertEquals(status, recommendationResponse.getStatus());
        Assert.assertEquals(seedItem, recommendationResponse.getSeedItem());
        Assert.assertEquals(collectionName, recommendationResponse.getCollection());
        Assert.assertEquals(source, recommendationResponse.getSource());
        Assert.assertEquals(maxRecommendations, recommendationResponse.getMaxRecommendations());

        List<Recommendation> returnedRecommendations = recommendationResponse.getRecommendations();

        if (returnedRecommendations != null) {
            actualSize = returnedRecommendations.size();
        }

        Assert.assertEquals(expectedSize, actualSize);

        if (expectedSize > 0) {
            Recommendation returnedRecommendation = returnedRecommendations.get(0);
            Assert.assertEquals(recommendedItem, returnedRecommendation.getItemID());
            Assert.assertEquals(recommendedItemTitle, returnedRecommendation.getTitle());
        }
    }

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

    private SearchQuestion getMockSearchQuestion(String collectionName) {
        return getMockSearchQuestion(collectionName, com.funnelback.common.config.Collection.Type.web);
    }

    /**
     * Return a mock SearchQuestion for the given collection name.
     *
     * @param collectionName name of collection
     * @param collectionType type of collection (e.g. 'web', 'meta' etc.)
     * @return a mock SearchQuestion
     */
    private SearchQuestion getMockSearchQuestion(String collectionName,
                                                 com.funnelback.common.config.Collection.Type collectionType) {
        configRepository.removeAllCollections();
        Config collectionConfig = new NoOptionsConfig(collectionName);
        collectionConfig.setValue(Keys.COLLECTION_TYPE, collectionType.toString());
        collectionConfig.setValue(Keys.COLLECTION, collectionName);
        Collection collection = new Collection(collectionName, collectionConfig);
        configRepository.addCollection(collection);
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collection);

        return sq;
    }

    /**
     * Build and return a list of ItemTuples based on the given list of item IDs and associated titles.
     */
    private List<ItemTuple> getItemTuples(List<String> itemIDs, List<String> titles) {
        List<ItemTuple> itemTuples = new ArrayList<>();

        for (int i=0; i < itemIDs.size(); i++) {
            ItemTuple itemTuple = new ItemTuple(itemIDs.get(i), DEFAULT_SCORE, titles.get(i), null);
            itemTuples.add(itemTuple);
        }

        return itemTuples;
    }

    /**
     * Get a list of recommendations from the given list of ItemTuples.
     */
    private List<Recommendation> getRecommendations(List<ItemTuple> itemTuples) {
        List<Recommendation> recommendations = new ArrayList<>();

        for (ItemTuple itemTuple : itemTuples) {
            DocInfo docInfo = getMockDocInfo(itemTuple.getItemID(), itemTuple.getTitle());
            Recommendation recommendation = new Recommendation(itemTuple.getItemID(), DEFAULT_CONFIDENCE, docInfo, null);
            recommendations.add(recommendation);
        }

        return recommendations;
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
                "", MAX_RECOMMENDATIONS, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingExploreCollection() throws Exception {
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.exploreItems(request, response, sq, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingExploreQuery() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        sq.setCollection(collection);

        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.exploreItems(request, response, sq, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingCollectionParameter() throws Exception {
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM, "",
                MAX_RECOMMENDATIONS, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSourceParameter() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null,
                DEFAULT_SEED_ITEM, "", MAX_RECOMMENDATIONS, ItemTuple.Source.NONE.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSeedItem() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        MockHttpServletResponse response = new MockHttpServletResponse();
        recommenderController.similarItems(request, response, sq, null, "", "", MAX_RECOMMENDATIONS, null);
    }

    @Test
    public void testSpringExceptionHandler() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Exception exception = new IllegalArgumentException("Test exception");

        ModelAndView mav = recommenderController.exceptionHandler(response, exception);
        Map<String, String> errorResponse = (Map<String, String>) mav.getModel().get("RecommendationResponse");

        Assert.assertEquals(RecommendationResponse.Status.ERROR.toString(), errorResponse.get("status"));
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

        checkSimilarItems(sq, null, 1, RecommendationResponse.Status.OK, MAX_RECOMMENDATIONS, "", DEFAULT_COLLECTION_NAME);
    }

    @Test
    public void testMetaCollection() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(META_COLLECTION_NAME,
                com.funnelback.common.config.Collection.Type.meta);
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
        recommenderController.setConfigRepository(configRepository);

        checkSimilarItems(sq, null, 1, RecommendationResponse.Status.OK,
                MAX_RECOMMENDATIONS, "", META_COLLECTION_NAME);
    }

    @Test
    public void testExploreRecommendations() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        int maxRecommendations = 3;
        String scope = "";

        List<String> indexURLs = new ArrayList<>();
        indexURLs.add(EXPLORE_RECOMMENDATION);
        List<String> indexURLTitles = new ArrayList<>();
        indexURLTitles.add(EXPLORE_RECOMMENDATION_TITLE);

        List<ItemTuple> cachedItems = getItemTuples(indexURLs, indexURLTitles);
        List<Recommendation> recommendations = getRecommendations(cachedItems);

        RecommendationResponse simulatedResponse = new RecommendationResponse(RecommendationResponse.Status.OK,
                EXPLORE_SEED, DEFAULT_COLLECTION_NAME, scope, maxRecommendations, collectionConfig.getCollectionName(),
                ItemTuple.Source.EXPLORE, -1, recommendations);

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

        checkResponse(recommendationResponse, EXPLORE_SEED, DEFAULT_COLLECTION_NAME,
                ItemTuple.Source.EXPLORE, maxRecommendations, EXPLORE_RECOMMENDATION,
                EXPLORE_RECOMMENDATION_TITLE, 1, RecommendationResponse.Status.OK);

        // Text maxRecommendations not being set in direct explore query (should fall back to internal default)
        String exploreQuery = RecommenderController.EXPLORE_QUERY_PREFIX + EXPLORE_SEED;
        sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        sq.setQuery(exploreQuery);
        mav = recommenderController.exploreItems(request, response, sq, null);
        recommendationResponse =
                        (RecommendationResponse) mav.getModel().get("RecommendationResponse");

        checkResponse(recommendationResponse, EXPLORE_SEED, DEFAULT_COLLECTION_NAME,
                ItemTuple.Source.EXPLORE, maxRecommendations, EXPLORE_RECOMMENDATION,
                EXPLORE_RECOMMENDATION_TITLE, 1, RecommendationResponse.Status.OK);
    }

    @Test
    public void testExploreNoSuggestions() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();

        SearchController searchController = mock(SearchController.class);
        when(searchController.search(any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(SearchQuestion.class), any(SearchUser.class))).thenReturn(null);
        recommenderController.setSearchController(searchController);

        String exploreQuery = "explore:" + DEFAULT_SEED_ITEM;
        sq.setQuery(exploreQuery);

        DataAPI dataAPI = mock(DataAPI.class);
        DocInfo seedDocInfo = getMockDocInfo(DEFAULT_SEED_ITEM, DEFAULT_SEED_ITEM_TITLE);
        when(dataAPI.getDocInfo(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(seedDocInfo);
        recommenderController.setDataAPI(dataAPI);

        // Return no cached recommendations to force an explore request to be run.
        RecommenderDAO recommenderDAO = mock(RecommenderDAO.class);
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(null);
        recommenderController.setRecommenderDAO(recommenderDAO);

        checkSimilarItems(sq, ItemTuple.Source.DEFAULT.toString(), 0,
                RecommendationResponse.Status.NO_SUGGESTIONS_FOUND, MAX_RECOMMENDATIONS, "", DEFAULT_COLLECTION_NAME);
    }

    @Test
    public void testPositiveScope() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        String scope = "careers";

        // Include an item that is out of scope in the list of cached itemIDs, and generate a "scoped itemIDs"
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

        checkSimilarItems(sq, ItemTuple.Source.DEFAULT.toString(), 1,
                RecommendationResponse.Status.OK, MAX_RECOMMENDATIONS, scope, DEFAULT_COLLECTION_NAME);
    }

    @Test
    public void testNegativeScope() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        String scope = "-hr";

        // Include an item that is out of scope in the list of cached itemIDs, and generate a "scoped itemIDs"
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

        checkSimilarItems(sq, ItemTuple.Source.CLICKS.toString(), 1,
                RecommendationResponse.Status.OK, MAX_RECOMMENDATIONS, scope, DEFAULT_COLLECTION_NAME);
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

        // Also test null scope parameter
        checkSimilarItems(sq, ItemTuple.Source.CLICKS.toString(), 1,
                RecommendationResponse.Status.OK, maxRecommendations, null, DEFAULT_COLLECTION_NAME);
    }

    /**
     * Test sourceType (Default, Clicks, Explore, None).
     */
    @Test
    public void testSource() throws Exception {
        SearchQuestion sq = getMockSearchQuestion(DEFAULT_COLLECTION_NAME);
        Collection collection = sq.getCollection();
        Config collectionConfig = collection.getConfiguration();
        String scope = "";

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

        checkSimilarItems(sq, ItemTuple.Source.DEFAULT.toString(), 2,
                RecommendationResponse.Status.OK, MAX_RECOMMENDATIONS, scope, DEFAULT_COLLECTION_NAME);
        checkSimilarItems(sq, ItemTuple.Source.CLICKS.toString(), 2,
                RecommendationResponse.Status.OK, MAX_RECOMMENDATIONS, scope, DEFAULT_COLLECTION_NAME);

        // Set things up for an Explore source by now returning a smaller list in response to an
        // explicit Explore request
        recommendations.remove(1);
        RecommendationResponse simulatedResponse = new RecommendationResponse(RecommendationResponse.Status.OK,
                DEFAULT_SEED_ITEM, DEFAULT_COLLECTION_NAME, scope, MAX_RECOMMENDATIONS,
                collectionConfig.getCollectionName(), ItemTuple.Source.EXPLORE, -1, recommendations);
        when(dataAPI.getResponseFromResults(any(String.class), any(List.class), any(Config.class),
                any(String.class), any(String.class), anyInt())).thenReturn(simulatedResponse);

        checkSimilarItems(sq, ItemTuple.Source.EXPLORE.toString(), 1,
                RecommendationResponse.Status.OK, MAX_RECOMMENDATIONS, scope, DEFAULT_COLLECTION_NAME);

        // Now configure DAO to return nothing for the given seed item i.e. no clicked recommendations available
        // This is so we can confirm that it does not fall through to returning Explore results (default behaviour).
        when(recommenderDAO.getRecommendations(DEFAULT_SEED_ITEM, collectionConfig)).thenReturn(null);
        checkSimilarItems(sq, ItemTuple.Source.CLICKS.toString(), 0,
                RecommendationResponse.Status.NO_SUGGESTIONS_FOUND, MAX_RECOMMENDATIONS, scope, DEFAULT_COLLECTION_NAME);
    }
}