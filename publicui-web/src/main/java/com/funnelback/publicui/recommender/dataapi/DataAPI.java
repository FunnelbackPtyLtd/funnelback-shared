package com.funnelback.publicui.recommender.dataapi;

import com.funnelback.common.config.Config;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.RecommendationResponse;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.reporting.recommender.tuple.ItemTuple;

import java.util.List;
import java.util.Map;

/**
 *  The DataAPI interface provides access to the Funnelback Data API. This is mainly used to "decorate"
 *  suggestions returned by the Recommender with information from the Data API (e.g. title, metadata etc.)
 */
public interface DataAPI {
    /**
     * Return a list of URL recommendations which have been "decorated" with information from the Data API/libi4u.
     *
     *
     * @param urls               list of URL strings to decorate
     * @param confidenceMap      Optional map of urls to confidence scores (can be null if not available).
     * @param collectionConfig   collection config object
     * @return list of decorated URL recommendations (which may be empty)
     */
    List<Recommendation> decorateURLRecommendations(List<String> urls,
                                                    Map<String, ItemTuple> confidenceMap, Config collectionConfig);

    /**
     * Return a DocInfoResult for the given URL items in the given collection. Callers can call asList()
     * or asMap() on the result to get the data in the format they need.
     * Document information for any URLs which are not in the index will not be
     * present in the returned object.
     *
     * @param urls             list of URLs
     * @param collectionConfig collection config object
     * @return a DocInfoResult (which may be null).
     */
    DocInfoResult getDocInfoResult(List<String> urls, Config collectionConfig);

    /**
     * Return a DocInfo object for a single URL.
     *
     * @param url              URL string to get DocInfo for
     * @param collectionConfig collection config object
     * @return DocInfo object (may be null if unable to get information)
     */
    DocInfo getDocInfo(String url, Config collectionConfig);

    /**
     * Return a RecommendationResponse built from the given list of results (which came from an 'explore:url' query.
     *
     * @param seedItem seed URL
     * @param results list of results from explore query
     * @param collectionConfig collection config object
     * @param requestCollection name of the collection that the original recommendation request was made to
     * @param scope scope parameter (may be empty)
     * @param maxRecommendations maximum number of recommendations to return
     * 
     */
    RecommendationResponse getResponseFromResults(String seedItem, List<Result> results,
                                                              Config collectionConfig, String requestCollection,
                                                              String scope, int maxRecommendations);

    /**
     * Return the title of the given URL from the given collection.
     *
     * @param url              URL to get title for
     * @param collectionConfig collection Config object
     * @return title or empty string if title is not available
     */
    String getTitle(String url, Config collectionConfig);
}
