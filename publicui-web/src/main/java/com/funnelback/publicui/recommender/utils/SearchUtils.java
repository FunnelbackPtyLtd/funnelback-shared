package com.funnelback.publicui.recommender.utils;

import com.funnelback.common.config.Config;
import com.funnelback.common.utils.ObjectMapperSingleton;
import com.funnelback.common.utils.StringCount;
import com.funnelback.reporting.DatabaseAccess;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import com.funnelback.reporting.recommender.utils.RecommenderUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides utilities for interacting with the Funnelback Search Service as
 * part of the Recommender lifecycle.
 *
 * @author fcrimmins@funnelback.com
 */
public final class SearchUtils {
    private static final Logger logger = Logger.getLogger(SearchUtils.class);
    public static final String EXPLORE_QUERY_PREFIX = "explore:";
    public static final int DEFAULT_NUM_RANKS = 30;

    private Config collectionConfig;

    public SearchUtils(Config collectionConfig) {
        this.collectionConfig = collectionConfig;
    }

    /**
     * Get search results for the given query and search URL. Each result is a Map which can be queried
     * like: result.get("liveUrl") or result.get("title");
     *
     * @param query         search terms.
     * @param searchService search service URL
     * @param scope list of scopes to apply to queries
     * @param numRanks maximum number of results to return (less than this may be available).
     * @return List of results from the search engine result packet.
     */
    public List<Map<String, Object>> getResults(String query, String searchService,
            String scope, int numRanks) throws IOException {
        List<Map<String, Object>> results = null;
        HttpURLConnection urlConnection = null;
        ObjectMapper mapper = ObjectMapperSingleton.getInstance();

        if (scope != null && !("").equals(scope)) {
            String utf8Scope = "";

            try {
                utf8Scope = URLEncoder.encode(scope, "utf-8");
            } catch (UnsupportedEncodingException exception) {
                logger.warn(exception);
            }

            if (!("").equals(utf8Scope)) {
                searchService = searchService + "&scope=" + utf8Scope;
            }
        }

        if (numRanks < 1) {
            numRanks = DEFAULT_NUM_RANKS;
        }

        searchService = searchService + "&numRanks=" + numRanks;

        if (query != null && !query.trim().equals("")) {
            try {
                URL url = new URL(searchService + "&query=" + URLEncoder.encode(query, "utf-8"));
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // Drill down through the JSON to get to the result packet
                Map<String, Object> jsonMap = mapper.readValue(in, Map.class);
                Map<String, Object> response = (Map<String, Object>) jsonMap.get("response");
                Map<String, Object> resultPacket = (Map<String, Object>) response.get("resultPacket");
                results = (List<Map<String, Object>>) resultPacket.get("results");
            } catch (NullPointerException nullPointerException) {
                logger.error("FBRecommenderREST.getResults(): " + nullPointerException);
            } catch (UnsupportedEncodingException exception) {
                logger.error("FBRecommenderREST.getResults(): " + exception);
            } finally {
                urlConnection.disconnect();
            }
        }

        return results;
    }

    /**
     * Get recommendations from the specified source (currently supported sources are RELATED_RESULTS
     * and EXPLORE_RESULTS).
     * @param itemName name (ID) of item.
     * @param searchService address of search service to send queries to
     * @param scope list of scopes to apply to queries
     * @param source   Enum specifying source of recommendations: DEFAULT (blended mixture), CO_CLICKS,
     *                 RELATED_CLICKS, RELATED_RESULTS, EXPLORE_RESULTS.
     * @return list of recommendations (which may be empty but not null).
     */
    public List<ItemTuple> getRecommendationsFromSource(String itemName,
                String searchService, String scope, ItemTuple.Source source) {
        if (source.equals(ItemTuple.Source.RELATED_RESULTS)) {
            return getRelatedResults(itemName, searchService, scope);
        }
        else if (source.equals(ItemTuple.Source.EXPLORE_RESULTS)) {
            return getExploreResults(itemName, searchService, scope);
        }

        return new ArrayList<>();
    }

    /**
     * Return a list of related results for the given item (URL), by running the top N queries
     * that result in clicks on that URL, and appending each result list to the overall list.
     * A later sortList() call should be used to sort the list and boost those items which
     * appear in more than one result list.
     *
     * @param itemName item to get related results for
     * @param searchService address of search service to send queries to
     * @param scope list of scopes to apply to queries
     * @return list of ItemTuples (which may be empty but not null).
     */
    private List<ItemTuple> getRelatedResults(String itemName,
            String searchService, String scope) {
        List<ItemTuple> relatedResults = new ArrayList<>();
        DatabaseAccess dba = null;
        String installDir = collectionConfig.getSearchHomeDir().getAbsolutePath();
        String collectionName = collectionConfig.getCollectionName();

        try {
            dba = new DatabaseAccess(new File(installDir), collectionName, false);
            List<StringCount> relatedQueries = dba.selectQueriesForURLWithCounts(itemName);

            if (relatedQueries != null && relatedQueries.size() > 0) {

                if (relatedQueries.size() > RecommenderUtils.MAX_RELATED_ITEMS) {
                    relatedQueries = relatedQueries.subList(0, RecommenderUtils.MAX_RELATED_ITEMS - 1);
                }

                for (StringCount relatedQuery : relatedQueries) {
                    String query = relatedQuery.getString();
                    logger.debug("About to run query: " + query);
                    List<Map<String, Object>> results = null;

                    try {
                        results = getResults(query, searchService, scope, DEFAULT_NUM_RANKS);
                    } catch (IOException ioe) {
                        logger.warn(ioe);
                    }

                    if (results != null && !results.isEmpty()) {
                        int i = 0;

                        for (Map<String, Object> result : results) {
                            String resultURL = (String) result.get("liveUrl");
                            resultURL = resultURL.trim();

                            if (!itemName.equals(resultURL)) {
                                ItemTuple itemTuple = new ItemTuple(resultURL, ItemTuple.Source.RELATED_RESULTS);
                                itemTuple.setRank(i);
                                relatedResults.add(itemTuple);
                                i++;
                            }
                        }
                    }
                }
            } else {
                logger.debug("No related queries found for item: " + itemName);
            }
        } catch (Exception exception) {
            logger.warn("Error getting blended queries for item: " + itemName + " - " + exception);
        } finally {
            if (dba != null) {
                try {
                    dba.finish();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return relatedResults;
    }

    /**
     * Return a list of "explore:" results for the given item (URL).
     *
     * @param itemName item to get related results for
     * @param searchService address of search service to send queries to
     * @param scope list of scopes to apply to explore query
     * @return list of ItemTuples (which may be empty but not null).
     */
    private List<ItemTuple> getExploreResults(String itemName,
            String searchService, String scope) {
        List<ItemTuple> exploreResults = new ArrayList<>();
        String exploreQuery = EXPLORE_QUERY_PREFIX + itemName;
        List<Map<String, Object>> results = null;

        try {
            results = getResults(exploreQuery, searchService, scope, DEFAULT_NUM_RANKS);
        } catch (IOException ioe) {
            logger.warn(ioe);
        }

        if (results != null && !results.isEmpty()) {
            int i = 0;

            for (Map<String, Object> result : results) {
                String resultURL = (String) result.get("liveUrl");
                resultURL = resultURL.trim();

                if (!itemName.equals(resultURL)) {
                    ItemTuple itemTuple = new ItemTuple(resultURL, ItemTuple.Source.EXPLORE_RESULTS);
                    itemTuple.setRank(i);
                    exploreResults.add(itemTuple);
                    i++;
                }
            }
        }

        return exploreResults;
    }
}