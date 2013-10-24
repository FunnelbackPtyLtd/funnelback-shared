package com.funnelback.publicui.recommender.dataapi;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.RecommendationResponse;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DataAPIConnectorPADRE implements the DataAPI interface, which provides access to the Funnelback Data API.
 * This is mainly used to "decorate" suggestions returned by the Recommender with information from the Data API
 * (e.g. title, metadata etc.)
 */
@Component
public class DataAPIConnectorPADRE implements DataAPI {
    private static final Logger logger = Logger.getLogger(DataAPIConnectorPADRE.class);
    
    /**
     * Return a list of URL recommendations which have been "decorated" with information from the Data API/libi4u.
     * The list returned should never be larger than the value of the "maxRecommendations" parameter.
     *
     *
     * @param urls               list of URL strings to decorate
     * @param confidenceMap      Optional map of urls to confidence scores (can be null if not available).
     * @param collectionConfig   collection configuration object
     * @return list of decorated URL recommendations (which may be empty)
     */
    public List<Recommendation> decorateURLRecommendations(List<String> urls,
            Map<String, ItemTuple> confidenceMap, Config collectionConfig) {
        List<Recommendation> recommendations = new ArrayList<>();
        float confidence = -1;
        List<DocInfo> dis = null;
        DocInfoResult dir = getDocInfoResult(urls, collectionConfig);

        if (dir != null) {
            dis = dir.asList();
        }

        if (dis != null && dis.size() > 0) {
            for (DocInfo docInfo : dis) {
                URI uri = docInfo.getUri();
                String itemID = uri.toString();

                if (confidenceMap != null) {
                    confidence = confidenceMap.get(itemID).getScore();
                }

                Recommendation recommendation = new Recommendation(itemID, confidence, docInfo);
                recommendations.add(recommendation);
            }
        } else {
            logger.warn("Null or empty DocInfo list returned from getDocInfoResult.");
        }

        return recommendations;
    }

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
    public DocInfoResult getDocInfoResult(List<String> urls, Config collectionConfig) {
        DocInfoResult dir = null;

        if (urls.size() > 0) {
            File indexStem = new File(collectionConfig.getCollectionRoot() + File.separator + DefaultValues.VIEW_LIVE
                    + File.separator + "idx" + File.separator + "index");

            URI[] addresses = new URI[urls.size()];
            int i = 0;
            for (String item : urls) {
                addresses[i] = URI.create(item);
                logger.debug("Added URL to list for libi4u call: " + addresses[i].toString());
                i++;
            }

            boolean debug = false;
            dir = new PadreConnector(indexStem).docInfo(addresses).withMetadata(DocInfoQuery.ALL_METADATA).withDebug(debug).fetch();
        } else {
            logger.warn("Empty list of URLs provided for query to collection: "
                    + collectionConfig.getCollectionName());
        }

        return dir;
    }

    /**
     * Return a DocInfo object for a single URL.
     *
     * @param url              URL string to get DocInfo for
     * @param collectionConfig collection config object
     * @return DocInfo object (may be null if unable to get information)
     */
    public DocInfo getDocInfo(String url, Config collectionConfig) {
        DocInfo docInfo = null;

        List<String> urls = new ArrayList<>();
        urls.add(url);

        DataAPIConnectorPADRE dataAPI = new DataAPIConnectorPADRE();
        List<DocInfo> dis = dataAPI.getDocInfoResult(urls, collectionConfig).asList();

        if (dis != null && dis.size() == 1) {
            docInfo = dis.get(0);
        }

        return docInfo;
    }

     /**
      * Return a RecommendationResponse built from the given list of results (which came from an 'explore:url' query).
      *
      * @param seedItem seed URL
      * @param results list of results from explore query
      * @param collectionConfig collection config object
      * @param requestCollection name of the collection that the original recommendation request was made to
      * @param scope scope parameter (may be empty)
      * @param maxRecommendations maximum number of recommendations to return
      * 
      */
     public RecommendationResponse getResponseFromResults(String seedItem, List<Result> results,
                                                          Config collectionConfig, String requestCollection,
                                                          String scope, int maxRecommendations) {
 		List<Recommendation> recommendations;
         List<String> urls = new ArrayList<>();

         for (Result result : results) {
             String indexUrl = result.getIndexUrl();
             urls.add(indexUrl);
         }

         if (scope == null) {
             scope = "";
         }

         DataAPIConnectorPADRE dataAPI = new DataAPIConnectorPADRE();
         recommendations = dataAPI.decorateURLRecommendations(urls, null, collectionConfig);

         if (recommendations != null && recommendations.size() > 0) {

             // Make sure number of recommendations is <= maxRecommendations
             if (recommendations != null && recommendations.size() > maxRecommendations) {
                 recommendations = recommendations.subList(0, maxRecommendations);
             }

             return new RecommendationResponse(RecommendationResponse.Status.OK, seedItem, requestCollection, scope, maxRecommendations,
                     collectionConfig.getCollectionName(), RecommendationResponse.Source.EXPLORE, -1, recommendations);
         }
         else {
             return new RecommendationResponse(RecommendationResponse.Status.NO_SUGGESTIONS_FOUND, seedItem, requestCollection, scope,
                     maxRecommendations, collectionConfig.getCollectionName(), RecommendationResponse.Source.NONE, -1, null);
         }
 	}

    /**
     * Return the title of the given URL from the given collection.
     *
     * @param url              URL to get title for
     * @param collectionConfig collection Config object
     * @return title or empty string if title is not available
     */
    public String getTitle(String url, Config collectionConfig) {
        String title = "";
        DataAPIConnectorPADRE dataAPI = new DataAPIConnectorPADRE();
        DocInfo docInfo = dataAPI.getDocInfo(url, collectionConfig);

        if (docInfo != null) {
            title = docInfo.getTitle();
        }

        return title;
    }
}
