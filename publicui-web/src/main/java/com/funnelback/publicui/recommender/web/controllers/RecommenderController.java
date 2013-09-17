package com.funnelback.publicui.recommender.web.controllers;

import com.funnelback.common.config.Config;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.RecommendationResponse;
import com.funnelback.publicui.recommender.compare.SortType;
import com.funnelback.publicui.recommender.utils.RecommenderUtils;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.controllers.SearchController;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

/**
 * This class represents the RESTful API to the Funnelback Recommendation System.
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

    @Autowired
    @Setter
    private ConfigRepository configRepository;

	public enum ModelAttributes {
		SearchTransaction, AllCollections, QueryString, SearchPrefix, ContextPath, Log,
		extraSearches, question, response, session, error, httpRequest;

		public static Set<String> getNames() {
			HashSet<String> out = new HashSet<>();
			for (ModelAttributes name: values()) {
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

	@Resource(name="jsonView")
	private View view;

    /**
	 * Return JSON output showing similar (recommended) items for the given item name.
	 *
     * @param seedItem name of seed item to get recommended items for
     * @param collection collection ID
     * @param scope comma separated list of scopes e.g. cmis.csiro.au,-vic.cmis.csiro.au
     * @param dsort descending sort parameter (optional)
     * @param asort ascending sort parameter (optional)
     * @return String containing recommendations, in JSON format
	 * @throws Exception
	 */
    @RequestMapping(value={"/" + SIMILAR_ITEMS_JSON}, method = RequestMethod.GET)
    public ModelAndView similarItems(HttpServletResponse response,
    		@RequestParam("seedItem") String seedItem,
    		@RequestParam("collection") String collection,
    		@RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "dsort", required = false) String dsort,
            @RequestParam(value = "asort", required = false) String asort,
            @RequestParam(value = "metadataClass", required = false) String metadataClass) throws Exception {
        Date startTime = new Date();
        response.setContentType("application/json");
        Comparator<Recommendation> comparator;
        RecommendationResponse recommendationResponse;
        Map<String, Object> model = new HashMap<>();

        if (seedItem == null || ("").equals(seedItem)) {
            throw new IllegalArgumentException("seedItem parameter must be provided.");
        }

        if (collection == null || ("").equals(collection)) {
            throw new IllegalArgumentException("collection parameter must be provided.");
        }

        if (metadataClass != null || ("").equals(metadataClass)) {
            if (!DocInfoQuery.isValidMetadataClass(metadataClass)) {
                throw new IllegalArgumentException("metadataClass parameter value is invalid: " + metadataClass);
            }
        }

        Collection collectionRef = configRepository.getCollection(collection);

        if (collectionRef != null) {
            Config collectionConfig = collectionRef.getConfiguration();

            comparator = SortType.getComparator(asort, dsort, metadataClass);

            List<Recommendation> recommendations =
                    RecommenderUtils.getRecommendationsForItem(seedItem, collectionConfig, scope, 5);
            long timeTaken = System.currentTimeMillis() - startTime.getTime();
            recommendationResponse =
          				new RecommendationResponse(RecommenderUtils.sortRecommendations(recommendations, comparator),
                                RecommendationResponse.Source.clicks, timeTaken);
          	model.put("RecommendationResponse", recommendationResponse);
        }

        return new ModelAndView(view, model);
    }

	@RequestMapping(value={"/" + EXPLORE_JSON}, method = RequestMethod.GET, params={RequestParameters.COLLECTION})
	public ModelAndView similarItems(HttpServletRequest request, HttpServletResponse response,
			@Valid SearchQuestion question, @ModelAttribute SearchUser user) throws Exception {
        Date startTime = new Date();
        RecommendationResponse recommendationResponse;
        response.setContentType("application/json");
		
		Map<String, Object> model;
		{
			ModelAndView modelandView = searchController.search(request, response, question, user);
			if (modelandView == null){
				return null;
			}
			model = modelandView.getModel();
		}
		SearchResponse searchResponse = (SearchResponse) model.get((SearchController.ModelAttributes.response.toString()));
        recommendationResponse = RecommendationResponse.fromResults(searchResponse.getResultPacket().getResults());
        long timeTaken = System.currentTimeMillis() - startTime.getTime();
        recommendationResponse.setTimeTaken(timeTaken);
		model.put("RecommendationResponse", recommendationResponse);
		
		return new ModelAndView(view, model);
	}
}