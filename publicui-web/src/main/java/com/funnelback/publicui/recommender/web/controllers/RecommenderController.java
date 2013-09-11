package com.funnelback.publicui.recommender.web.controllers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.recommender.FBRecommender;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.RecommendationResponse;
import com.funnelback.publicui.recommender.SortType;
import com.funnelback.publicui.recommender.utils.RecommenderUtils;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.controllers.SearchController;

/**
 * This class represents the RESTful API to the Funnelback Recommendation System.
 * @author fcrimmins@funnelback.com
 */
@Controller
@RequestMapping("/recommender")
public class RecommenderController {
	private static FBRecommender fbRecommender;
	public static final String searchRecommendationsHtml = "searchRecommendations.html";
	public static final String queryEntryHtml = "queryEntry.html";
	public static final String itemEntryHtml = "itemEntry.html";
	public static final String similarItemsJson = "similarItems.json";
	public static final String explorerJson = "explore.json";
	public static final String sessionsHtml = "sessions.html";

	static {
		// Get a single instance of the Recommender.
		fbRecommender = FBRecommender.getInstance();
	}

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
	private LocaleResolver localeResolver;
	
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
	@RequestMapping(value={"/" + similarItemsJson}, method = RequestMethod.GET)
	public ModelAndView similarItems(HttpServletResponse response,
			@RequestParam("seedItem") String seedItem,
			@RequestParam("collection") String collection,
			@RequestParam(value = "scope", required = false) String scope,
			@RequestParam(value = "dsort", required = false) String dsort,
			@RequestParam(value = "asort", required = false) String asort,
			@RequestParam(value = "metadataClass", required = false) String metadataClass) throws Exception {
		response.setContentType("application/json");
		Comparator<Recommendation> comparator;

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

		comparator = SortType.getComparator(asort, dsort, metadataClass);

		List<Recommendation> recommendations =
				RecommenderUtils.getRecommendationsForItem(fbRecommender, seedItem, collection, scope, 5);

		RecommendationResponse recommendationResponse =
				new RecommendationResponse(RecommenderUtils.sortRecommendations(recommendations, comparator));
		Map<String, Object> model = new HashMap<>();
		model.put("RecommendationResponse", recommendationResponse);

		return new ModelAndView(view, model);
	}

	@RequestMapping(value={"/" + explorerJson}, method = RequestMethod.GET, params={RequestParameters.COLLECTION})
	public ModelAndView similarItems(HttpServletRequest request,
			HttpServletResponse response ,
			@Valid SearchQuestion question,
			@ModelAttribute SearchUser user
			) throws Exception {
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
		model.put("RecommendationResponse", RecommendationResponse.fromResults(searchResponse.getResultPacket().getResults()));
		
		return new ModelAndView(view, model);
	}
}