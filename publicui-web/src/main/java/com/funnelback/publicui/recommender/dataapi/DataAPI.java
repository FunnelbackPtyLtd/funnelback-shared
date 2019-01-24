package com.funnelback.publicui.recommender.dataapi;

import com.funnelback.common.config.Config;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.reporting.recommender.tuple.ItemTuple;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * The DataAPI interface provides access to the Funnelback Data API. This is mainly used to "decorate"
 * suggestions returned by the Recommender with information from the Data API (e.g. title, metadata etc.).
 * @author fcrimmins@funnelback.com
 */
public interface DataAPI {
    /**
     * Return a list of URL recommendations which have been "decorated" with information from the Data API/libi4u.
     * @param uris               list of URIs to decorate
     * @param confidenceMap      Optional map of urls to confidence scores (can be null if not available).
     * @param collectionConfig   collection config object
     * @return list of decorated URL recommendations (which may be empty)
     */
    List<Recommendation> decorateURLRecommendations(List<URI> uris,
                                                    Map<String, ItemTuple> confidenceMap, Config collectionConfig);

    /**
     * Return a DocInfoResult for the given URL items in the given collection. Callers can call asList()
     * or asMap() on the result to get the data in the format they need.
     * Document information for any URLs which are not in the index will not be
     * present in the returned object.
     * @param uris             list of URIs
     * @param collectionConfig collection config object
     * @return a DocInfoResult (which may be null).
     */
    DocInfoResult getDocInfoResult(List<URI> uris, Config collectionConfig);

    /**
     * Return a DocInfo object for a single URL.
     * @param url              URL string to get DocInfo for
     * @param collectionConfig collection config object
     * @return DocInfo object (may be null if unable to get information)
     */
    DocInfo getDocInfo(String url, Config collectionConfig);

    /**
     * Return the title of the given URL from the given collection.
     * @param url              URL to get title for
     * @param collectionConfig collection Config object
     * @return title or empty string if title is not available
     */
    String getTitle(String url, Config collectionConfig);
}
