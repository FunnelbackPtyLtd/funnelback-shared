package com.funnelback.publicui.recommender.web.controllers;

import com.funnelback.common.config.Config;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.RecommendationResponse;
import com.funnelback.publicui.recommender.compare.SortType;
import com.funnelback.publicui.recommender.utils.RecommenderUtils;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.controllers.SearchController;
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
 *
 * @author fcrimmins@funnelback.com
 */
@Controller
@RequestMapping("/recommender")
public class RecommenderController {
    public static final String SEARCH_RECOMMENDATIONS_HTML = "searchRecommendations.html";
    public static final String QUERY_ENTRY_HTML = "queryEntry.html";
    public static final String ITEM_ENTRY_HTML = "itemEntry.html";
    public static final String SIMILAR_ITEMS_JSON = "similarItems.json";
    public static final String SESSIONS_HTML = "sessions.html";
    public static final String EXPLORE_JSON = "explore.json";

    public enum ModelAttributes {
        SearchTransaction, AllCollections, QueryString, SearchPrefix, ContextPath, Log,
        extraSearches, question, response, session, error, httpRequest;

        public static Set<String> getNames() {
            HashSet<String> out = new HashSet<>();
            for (ModelAttributes name : values()) {
                out.add(name.toString());
            }
            return out;
        }
    }

    @Autowired
    private SearchController searchController;

    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
    }

    @Resource(name = "recommenderJsonView")
    private View view;

    /**
     * Return JSON output showing similar (recommended) items for the given item name.
     * @param request request from the client
     * @param response response to be sent back to the client
     * @param question a search question containing a reference to the collection etc.
     * @param user a search user
     * @param seedItem name of seed item to get recommended items for
     * @param scope    comma separated list of scopes e.g. cmis.csiro.au,-vic.cmis.csiro.au
     * @param dsort    descending sort parameter (optional)
     * @param asort    ascending sort parameter (optional)
     * @return String containing recommendations, in JSON format
     * @throws Exception
     */
    @RequestMapping(value = {"/" + SIMILAR_ITEMS_JSON}, method = RequestMethod.GET, params = {RequestParameters.COLLECTION})
    public ModelAndView similarItems(HttpServletRequest request, HttpServletResponse response,
                                     @Valid SearchQuestion question, @ModelAttribute SearchUser user,
                                     @RequestParam("seedItem") String seedItem,
                                     @RequestParam(value = "scope", required = false) String scope,
                                     @RequestParam(value = "dsort", required = false) String dsort,
                                     @RequestParam(value = "asort", required = false) String asort,
                                     @RequestParam(value = "metadataClass", required = false) String metadataClass) throws Exception {
        Date startTime = new Date();
        response.setContentType("application/json");
        Comparator<Recommendation> comparator;
        RecommendationResponse recommendationResponse;
        Map<String, Object> model = new HashMap<>();
        List<Recommendation> recommendations;
        com.funnelback.publicui.search.model.collection.Collection collection = question.getCollection();

        if (seedItem == null || ("").equals(seedItem)) {
            throw new IllegalArgumentException("seedItem parameter must be provided.");
        }

        if (collection == null) {
            throw new IllegalArgumentException("collection parameter must be provided.");
        }

        if (metadataClass != null || ("").equals(metadataClass)) {
            if (!DocInfoQuery.isValidMetadataClass(metadataClass)) {
                throw new IllegalArgumentException("metadataClass parameter value is invalid: " + metadataClass);
            }
        }

        Config collectionConfig = collection.getConfiguration();

        comparator = SortType.getComparator(asort, dsort, metadataClass);
        recommendations = RecommenderUtils.getRecommendationsForItem(seedItem, collectionConfig, scope, 5);

        if (recommendations == null || recommendations.size() == 0 && seedItem.startsWith("http")) {
            question.setQuery("explore:" + seedItem);
            // Any 'scope' parameter in the SearchQuestion will be passed through to PADRE and so Explore
            // suggestions should be automatically scoped.
            return exploreItems(request, response, question, user);
        }

        long timeTaken = System.currentTimeMillis() - startTime.getTime();

        recommendationResponse =
                new RecommendationResponse(seedItem,
                        RecommenderUtils.sortRecommendations(recommendations, comparator),
                        RecommendationResponse.Source.clicks, timeTaken);
        model.put("RecommendationResponse", recommendationResponse);

        return new ModelAndView(view, model);
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
        RecommendationResponse recommendationResponse;
        response.setContentType("application/json");
        com.funnelback.publicui.search.model.collection.Collection collection = question.getCollection();

        if (collection == null) {
            throw new IllegalArgumentException("collection parameter must be provided.");
        }

        Map<String, Object> model;
        {
            ModelAndView modelandView = searchController.search(request, response, question, user);
            if (modelandView == null) {
                return null;
            }
            model = modelandView.getModel();
        }
        SearchResponse searchResponse = (SearchResponse) model.get((SearchController.ModelAttributes.response.toString()));
        Config collectionConfig = collection.getConfiguration();
        recommendationResponse =
                RecommendationResponse.fromResults("", searchResponse.getResultPacket().getResults(), collectionConfig);
        long timeTaken = System.currentTimeMillis() - startTime.getTime();
        recommendationResponse.setTimeTaken(timeTaken);

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
        Map<String, Object> model = new HashMap<>();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.put("class", ClassUtils.getShortName(exception.getClass()));
        model.put("message", exception.getMessage());
        return new ModelAndView(view, model);
    }
}