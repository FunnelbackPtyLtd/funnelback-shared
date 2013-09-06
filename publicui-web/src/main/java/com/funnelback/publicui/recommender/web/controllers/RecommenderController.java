package com.funnelback.publicui.recommender.web.controllers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.recommender.FBRecommender;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.SortType;
import com.funnelback.publicui.recommender.utils.RecommenderUtils;
//import javax.ejb.Stateless;

/**
 * This class represents the RESTful API to the Funnelback Recommendation System.
 * @author fcrimmins@funnelback.com
 */
@Controller
@RequestMapping("/recommender")
public class RecommenderController {
    
    
	private static FBRecommender fbRecommender;
   
    public static final String RECOMMENDER_PREFIX = DefaultValues.ModernUI.CONTEXT_PATH + "/recommender/";
    public static final int MIN_CLICKS_PER_SESSION = 2;
    //private ObjectMapper mapper = ObjectMapperSingleton.getInstance();
	public static final String searchRecommendationsHtml = "searchRecommendations.html";
	public static final String queryEntryHtml = "queryEntry.html";
    public static final String itemEntryHtml = "itemEntry.html";
    public static final String similarItemsJson = "similarItems.json";
    public static final String sessionsHtml = "sessions.html";

	static {
		// Get a single instance of the Recommender.
        fbRecommender = FBRecommender.getInstance();
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
    		@RequestParam("scope") String scope,
    		@RequestParam("dsort") String dsort,
    		@RequestParam("asort") String asort,
    		@RequestParam("metadataClass") String metadataClass) throws Exception {
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
        List<Recommendation> sortedRecommendations
                = RecommenderUtils.sortRecommendations(recommendations, comparator);
         
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("soretedRecomendations", sortedRecommendations);
     
 		return new ModelAndView(view, model);
    }

}