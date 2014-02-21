package com.funnelback.publicui.recommender.dataapi;

import com.funnelback.common.View;
import com.funnelback.common.config.Config;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoAccess;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.Recommendation;
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
 *
 * @author fcrimmins@funnelback.com
 */
@Component
public class DataAPIConnectorPADRE implements DataAPI {
    private static final Logger logger = Logger.getLogger(DataAPIConnectorPADRE.class);

    /**
     * Return a list of URL recommendations which have been "decorated" with information from the Data API/libi4u.
     *
     * @param urls             list of URL strings to decorate
     * @param confidenceMap    Optional map of urls to confidence scores (can be null if not available).
     * @param collectionConfig collection configuration object
     * @return list of decorated URL recommendations (which may be empty)
     */
    public List<Recommendation> decorateURLRecommendations(List<String> urls,
            Map<String, ItemTuple> confidenceMap, Config collectionConfig) {
        List<Recommendation> recommendations = new ArrayList<>();
        List<DocInfo> dis = null;
        DocInfoResult dir = getDocInfoResult(urls, collectionConfig);

        if (dir != null) {
            dis = dir.asList();
        }

        if (dis != null && dis.size() > 0) {
            for (DocInfo docInfo : dis) {
                URI uri = docInfo.getUri();
                String itemID = uri.toString();
                ItemTuple.Source source = ItemTuple.Source.EXPLORE_RESULTS;
                ItemTuple itemTuple;

                if (confidenceMap != null) {
                    itemTuple = confidenceMap.get(itemID);
                }
                else {
                    itemTuple = new ItemTuple(itemID, source);
                }

                Recommendation recommendation = new Recommendation(itemTuple, docInfo);
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
        File indexStem = new File(collectionConfig.getCollectionRoot() + File.separator + View.live
                + File.separator + "idx" + File.separator + "index");
        return new DocInfoAccess().getDocInfoResult(urls, indexStem);
    }

    /**
     * Return a DocInfo object for a single URL.
     *
     * @param url              URL string to get DocInfo for
     * @param collectionConfig collection config object
     * @return DocInfo object (may be null if unable to get information)
     */
    public DocInfo getDocInfo(String url, Config collectionConfig) {
        File indexStem = new File(collectionConfig.getCollectionRoot() + File.separator + View.live
                + File.separator + "idx" + File.separator + "index");
        return new DocInfoAccess().getDocInfo(url, indexStem);
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
