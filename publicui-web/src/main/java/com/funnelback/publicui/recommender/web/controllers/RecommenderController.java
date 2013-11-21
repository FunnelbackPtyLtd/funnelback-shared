package com.funnelback.publicui.recommender.web.controllers;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.RecommendationResponse;
import com.funnelback.publicui.recommender.Recommender;
import com.funnelback.publicui.recommender.dao.RecommenderDAO;
import com.funnelback.publicui.recommender.dataapi.DataAPI;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.search.web.controllers.session.SessionController;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import lombok.Setter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

/**
 * This class represents the RESTful API to the Funnelback Recommendation System.
 * It extends SessionController so that we can have a unique user/session ID generated for requests.
 *
 * @author fcrimmins@funnelback.com
 */
@Controller
@RequestMapping("/recommender")
public class RecommenderController extends SessionController {
    private static final Logger logger = Logger.getLogger(RecommenderController.class);

    public static final String SIMILAR_ITEMS_JSON = "similarItems.json";
    public static final String EXPLORE_JSON = "explore.json";
    public static final int MAX_RECOMMENDATIONS = 5;
    public static final String MAX_EXPLORE_RESULTS = "50";
    public static final String EXPLORE_QUERY_PREFIX = "explore:";

    @Autowired
    @Setter
    private ConfigRepository configRepository;

    @Autowired
    @Setter
    private SearchController searchController;

    @Autowired
    @Setter
    private DataAPI dataAPI;

    @Autowired
    @Setter
    private RecommenderDAO recommenderDAO;

    @InitBinder
    public void initBinder(DataBinder binder) {
        // Call searchController binder method to bind request parameters to Java objects.
        searchController.initBinder(binder);
    }

    @Resource(name = "recommenderJsonView")
    private View view;

    /**
     * Return JSON output showing similar (recommended) items for the given item name.
     * @param request HTTP request from the client
     * @param response HTTP response to be sent back to the client
     * @param question a search question containing a reference to the collection etc.
     * @param user a search user
     * @param seedItem name of seed item to get recommended items for
     * @param scope    comma separated list of scopes e.g. cmis.csiro.au,-vic.cmis.csiro.au
     * @param maxRecommendations maximum number of recommendations to return (less than this may be available).
     * @return String containing recommendations, in JSON format
     * @throws Exception
     */
    @RequestMapping(value = {"/" + SIMILAR_ITEMS_JSON}, method = RequestMethod.GET, params = {RequestParameters.COLLECTION})
    public ModelAndView similarItems(HttpServletRequest request, HttpServletResponse response,
                                     @Valid SearchQuestion question, @ModelAttribute SearchUser user,
                                     @RequestParam("seedItem") String seedItem,
                                     @RequestParam(value = "scope", required = false) String scope,
                                     @RequestParam(value = "maxRecommendations", required = false)
                                     Integer maxRecommendations,
                                     @RequestParam(value = "source", required = false) String source)
                                     throws Exception {
        Date startTime = new Date();
        response.setContentType("application/json");
        RecommendationResponse recommendationResponse;
        Map<String, Object> model = new HashMap<>();
        List<Recommendation> recommendations = new ArrayList<>();
        com.funnelback.publicui.search.model.collection.Collection collection = question.getCollection();
        ItemTuple.Source sourceType = ItemTuple.Source.DEFAULT;
        RecommendationResponse.Status status = RecommendationResponse.Status.SEED_NOT_FOUND;
        ItemTuple.Source recommendationsSource;
        long timeTaken = -1;

        if (seedItem == null || ("").equals(seedItem)) {
            throw new IllegalArgumentException("seedItem parameter must be provided.");
        }

        if (collection == null) {
            throw new IllegalArgumentException("invalid collection identifier");
        }

        if (maxRecommendations == null) {
            maxRecommendations = MAX_RECOMMENDATIONS;
        }

        if (scope == null) {
            scope = "";
        }

        if (source != null) {
            source = source.toUpperCase();
            sourceType = ItemTuple.Source.valueOf(source);
        }

        if (sourceType.equals(ItemTuple.Source.NONE)) {
            throw new IllegalArgumentException("Invalid source identifier in request");
        }
        else {
            recommendationsSource = sourceType;
        }

        String requestCollection = collection.getId();
        String sourceCollection = requestCollection;
        
        try {
            Recommender recommender = new Recommender(collection, dataAPI, recommenderDAO, seedItem, configRepository);
            Config collectionConfig = recommender.getCollectionConfig();
            
            if (collectionConfig != null) {
                sourceCollection = collectionConfig.getCollectionName();

                switch(sourceType) {
                    case DEFAULT:
                        recommendations
                                = recommender.getRecommendationsForItem(seedItem, scope, maxRecommendations, sourceType);
                        if (recommendations == null || recommendations.size() == 0 && seedItem.contains("://")) {
                            return getExploreSuggestions(request, response, question, user, seedItem, maxRecommendations, scope);
                        }
                        break;
                    case EXPLORE:
                        return getExploreSuggestions(request, response, question, user, seedItem, maxRecommendations, scope);
				    default:
                        recommendations
                                = recommender.getRecommendationsForItem(seedItem, scope, maxRecommendations, sourceType);
				        break;
                }

                timeTaken = System.currentTimeMillis() - startTime.getTime();

                if (recommendations != null && recommendations.size() > 0) {
                    status = RecommendationResponse.Status.OK;
                    recommendationsSource = sourceType;
                }
                else {
                    status = RecommendationResponse.Status.NO_SUGGESTIONS_FOUND;
                }
            }
            
        }
        catch (IllegalStateException exception) {
        	logger.warn(exception);
        }

        recommendationResponse = new RecommendationResponse(status, seedItem, requestCollection, scope,
                maxRecommendations, sourceCollection, recommendationsSource, timeTaken, recommendations);
        model.put("RecommendationResponse", recommendationResponse);

        return new ModelAndView(view, model);
    }

    private ModelAndView getExploreSuggestions(HttpServletRequest request, HttpServletResponse response,
                                               @Valid SearchQuestion question, @ModelAttribute SearchUser user,
                                               String seedItem, Integer maxRecommendations, String scope) throws Exception {
        String exploreQuery = EXPLORE_QUERY_PREFIX + seedItem;
        question.setQuery(exploreQuery);
        question.getInputParameterMap().put("num_ranks", maxRecommendations.toString());

        if (!("").equals(scope)) {
            question.getInputParameterMap().put("scope", scope);
        }

        // Any 'scope' parameter in the SearchQuestion will be passed through to PADRE and so Explore
        // suggestions should be automatically scoped.
        return exploreItems(request, response, question, user);
    }

    /**
     * Return a list of "explore" recommendations based on the given "explore:url" query. This may come from an
     * external HTTP request or via another controller (e.g. if no 'standard' recommendations were available).
     * Note that the "confidence" value for all Explore suggestions is set to -1 to indicate that this information
     * is not available.
     * @param request request from the client
     * @param response response to be sent back to the client
     * @param question a search question containing a reference to the collection etc.
     * @param user a search user
     * @return ModelAndView containing a RecommendationResponse (which may be empty)
     * @throws Exception
     */
    @RequestMapping(value = {"/" + EXPLORE_JSON}, method = RequestMethod.GET, params = {RequestParameters.COLLECTION})
    public ModelAndView exploreItems(HttpServletRequest request, HttpServletResponse response,
                                     @Valid SearchQuestion question, @ModelAttribute SearchUser user) throws Exception {
        Date startTime = new Date();
        RecommendationResponse recommendationResponse = null;
        List<Recommendation> recommendations = null;
        response.setContentType("application/json");
        com.funnelback.publicui.search.model.collection.Collection collection = question.getCollection();
        Integer maxRecommendations;

        if (collection == null) {
            throw new IllegalArgumentException("collection parameter must be provided.");
        }

        String requestCollection = collection.getId();
        String query = question.getQuery();

        if (query == null || !query.startsWith(EXPLORE_QUERY_PREFIX)) {
            throw new IllegalArgumentException("Valid 'explore:url' query parameter must be provided.");
        }

        String seedItem = query.replaceFirst("^" + EXPLORE_QUERY_PREFIX, "");

        try {
            maxRecommendations = Integer.parseInt(question.getInputParameterMap().get("num_ranks"));
        }
        catch (NumberFormatException exception) {
            // Ignore - use default
            maxRecommendations = MAX_RECOMMENDATIONS;
        }

        question.getInputParameterMap().put("num_ranks", MAX_EXPLORE_RESULTS);
        String scope = question.getInputParameterMap().get("scope");

        if (scope == null) {
            scope = "";
        }

        Map<String, Object> model = null;
        {
            ModelAndView modelandView = searchController.search(request, response, question, user);
            if (modelandView == null) {
                logger.warn("Null model returned from search controller for query: " + query
                        + " and collection: " + requestCollection);
            }
            else {
                model = modelandView.getModel();
            }
        }

        if (model != null) {
            SearchResponse searchResponse = (SearchResponse) model.get((SearchController.ModelAttributes.response.toString()));
            Config collectionConfig = collection.getConfiguration();

            if (searchResponse.hasResultPacket()) {
                recommendationResponse =
                        dataAPI.getResponseFromResults(seedItem, searchResponse.getResultPacket().getResults(),
                                collectionConfig, requestCollection, scope, maxRecommendations);
            }
        }

        long timeTaken = System.currentTimeMillis() - startTime.getTime();

        if (recommendationResponse != null) {
            recommendationResponse.setTimeTaken(timeTaken);
        }
        else {
            logger.warn("Null recommendationResponse returned from data API for seed: " + seedItem
                    + " and collection: " + requestCollection);
            RecommendationResponse.Status status = RecommendationResponse.Status.NO_SUGGESTIONS_FOUND;
            recommendationResponse = new RecommendationResponse(status, seedItem, requestCollection, scope,
                    maxRecommendations, requestCollection, ItemTuple.Source.EXPLORE, timeTaken, recommendations);
        }

        Map<String, Object> recommendationModel = new HashMap<>();
        recommendationModel.put("RecommendationResponse", recommendationResponse);

        return new ModelAndView(view, recommendationModel);
    }

    /**
     * Handle exceptions thrown by other controllers.
     * @param response response object
     * @param exception exception that was thrown
     * @return ModelAndView containing error details for the client
     * @throws IOException
     */
    @ExceptionHandler
    public ModelAndView exceptionHandler(HttpServletResponse response, Exception exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", RecommendationResponse.Status.ERROR.toString());
        errorResponse.put("class", ClassUtils.getShortName(exception.getClass()));
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("stack-trace", ExceptionUtils.getStackTrace(exception));

        Map<String, Object> recommendationModel = new HashMap<>();
        recommendationModel.put("RecommendationResponse", errorResponse);
        return new ModelAndView(view, recommendationModel);
    }
}