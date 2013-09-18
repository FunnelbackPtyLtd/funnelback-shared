package com.funnelback.publicui.recommender.utils;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.utils.ObjectCache;
import com.funnelback.common.utils.SQLiteCache;
import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import com.funnelback.reporting.recommender.tuple.PreferenceTuple;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.net.URI;
import java.util.*;

/**
 * Miscellaneous recommender utility methods.
 *
 * @author fcrimmins@funnelback.com
 */
public final class RecommenderUtils {
    private static final Logger logger = Logger.getLogger(RecommenderUtils.class);

    // Private constructor to avoid unnecessary instantiation of the class
    private RecommenderUtils() {
    }

    /**
     * Sort the given list of recommendations using the specified {@link Comparator}
     *
     * @param recommendations list of {@link Recommendation}s
     * @param comparator      The comparator to apply
     * @return sorted list
     */
    public static List<Recommendation> sortRecommendations(List<Recommendation> recommendations,
                                                           Comparator<Recommendation> comparator) {
        List<Recommendation> sortedRecommendations;

        if (comparator != null && recommendations != null && !recommendations.isEmpty()) {
            int size = recommendations.size();
            Recommendation[] recommendationsArray = recommendations.toArray(new Recommendation[size]);
            Arrays.sort(recommendationsArray, comparator);
            sortedRecommendations = new ArrayList<>(Arrays.asList(recommendationsArray));
        } else {
            sortedRecommendations = recommendations;
        }

        return sortedRecommendations;
    }

    /**
     * Return a List of {@link com.funnelback.publicui.recommender.Recommendation}'s for the given item name.
     *
     * @param itemName           name of item
     * @param collectionConfig   collection config object
     * @param scope              comma separated list of items scopes
     * @param maxRecommendations maximum number of recommendations to display (less than 1 means unlimited)
     * @return List of recommendations (which may be empty).
     */
    public static List<Recommendation> getRecommendationsForItem(String itemName, Config collectionConfig,
                                                                 String scope, int maxRecommendations) {
        List<Recommendation> recommendations = new ArrayList<>();
        List<ItemTuple> fullList = null;
        List<String> scopes = new ArrayList<>();

        if (scope != null && !("").equals(scope)) {
            scopes = Arrays.asList(scope.split(","));
        }

        fullList = getRecommendationsFromCache(itemName, collectionConfig);

        if (fullList != null && fullList.size() > 0) {
            List<ItemTuple> scopedList = new ArrayList<>();

            int i = 0;
            for (ItemTuple item : fullList) {
                if (maxRecommendations > 0 && i >= maxRecommendations) {
                    break;
                }

                String itemValue = item.getItemID();

                if (itemValue != null && inScope(itemValue, scopes)) {
                    scopedList.add(item);
                    i++;
                }
            }

            Map<String, ItemTuple> map = new HashMap<>();
            List<String> urls = new ArrayList<>();

            for (ItemTuple item : scopedList) {
                String url = item.getItemID();

                map.put(url, item);
                urls.add(url);
            }

            List<DocInfo> dis = getDocInfoResult(urls, collectionConfig).asList();

            if (dis != null) {
                for (DocInfo docInfo : dis) {
                    URI uri = docInfo.getUri();
                    String itemID = uri.toString();
                    float confidence = map.get(itemID).getScore();
                    Recommendation recommendation = new Recommendation(itemID, confidence, docInfo);
                    recommendations.add(recommendation);
                }
            }
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
    public static DocInfoResult getDocInfoResult(List<String> urls, Config collectionConfig) {
        DocInfoResult dir;

        File indexStem = new File(collectionConfig.getCollectionRoot() + File.separator + DefaultValues.VIEW_LIVE
                + File.separator + "idx" + File.separator + "index");

        URI[] addresses = new URI[urls.size()];
        int i = 0;
        for (String item : urls) {
            addresses[i] = URI.create(item);
            i++;
        }

        dir = new PadreConnector(indexStem).docInfo(addresses).withMetadata(DocInfoQuery.ALL_METADATA).fetch();

        return dir;
    }

    /**
     * Return true if the given item is considered "in scope" based on the given list
     * of scope patterns (which may be empty).
     *
     * @param item   String to test for display
     * @param scopes list of scope patterns e.g. cmis.csiro.au,-vic.cmis.csiro.au
     * @return true if item should be displayed
     */
    public static boolean inScope(String item, List<String> scopes) {
        boolean inScope = false;

        if (scopes != null && scopes.size() > 0) {
            for (String scopePattern : scopes) {
                if (scopePattern.startsWith(("-"))) {
                    // Negative scope pattern i.e. -handbook.curtin.edu.au
                    scopePattern = scopePattern.substring(1, scopePattern.length());

                    if (item.contains(scopePattern)) {
                        inScope = false;
                        break;
                    }
                } else if (item.contains(scopePattern)) {
                    inScope = true;
                }
            }
        } else {
            inScope = true;
        }

        return inScope;
    }

    /**
     * Get a list of recommendations from a backing cache based on the given Config and hashName.
     *
     * @param key    key to lookup in cache
     * @param config collection config object
     * @return value as a list, which may be null
     */
    public static List<ItemTuple> getRecommendationsFromCache(String key, Config config) {
        List<ItemTuple> items = null;
        String databaseFilename
                = SQLiteCache.getDatabaseFilename(config, DefaultValues.VIEW_LIVE,
                com.funnelback.reporting.recommender.utils.RecommenderUtils.DATA_MODEL_HASH);
        File db = new File(databaseFilename);
        String value;

        if (db.exists()) {
            ObjectCache database = new SQLiteCache(databaseFilename, config, true);

            try {
                value = (String) database.get(key);

                if (value != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    // See http://wiki.fasterxml.com/JacksonInFiveMinutes#Data_Binding_with_Generics
                    items = mapper.readValue(value, new TypeReference<List<ItemTuple>>() {
                    });
                } else {
                    logger.warn("No value found in cache for key: " + key);
                }
            } catch (Exception exception) {
                logger.warn("Exception getting value and converting from JSON: " + exception);
            } finally {
                database.close();
            }
        }

        return items;
    }

    /**
     * Return a set of sessions that the given item appears in.
     *
     * @param itemName Name of item
     * @param config   Collection config object
     * @return set of sessions (which may be empty)
     */
    public static synchronized Set<List<PreferenceTuple>> getSessions(String itemName, Config config) {
        Set<List<PreferenceTuple>> sessions = new HashSet<>();
        String databaseFilename
                = SQLiteCache.getDatabaseFilename(config, DefaultValues.VIEW_LIVE,
                com.funnelback.reporting.recommender.utils.RecommenderUtils.SESSIONS_HASH);
        File db = new File(databaseFilename);
        String value;

        if (db.exists()) {
            ObjectCache database = new SQLiteCache(databaseFilename, config, true);

            try {
                value = (String) database.get(itemName);

                if (value != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    // See http://wiki.fasterxml.com/JacksonInFiveMinutes#Data_Binding_with_Generics
                    Set<String> sessionIDs = mapper.readValue(value, new TypeReference<Set<String>>() {});

                    if (sessionIDs != null && !sessionIDs.isEmpty()) {
                        for (String sessionID : sessionIDs) {
                            value = (String) database.get(sessionID);

                            if (value != null) {
                                List<PreferenceTuple> session
                                        = mapper.readValue(value, new TypeReference<List<PreferenceTuple>>() {});

                                if (session != null) {
                                    sessions.add(session);
                                }
                            }
                        }
                    } else {
                        logger.warn("No sessions found for item: " + itemName);
                    }
                } else {
                    logger.warn("No value found in cache for key: " + itemName);
                }
            } catch (Exception exception) {
                logger.warn("Exception getting value and converting from JSON: " + exception);
            } finally {
                database.close();
            }
        }

        return sessions;
    }

    /**
     * Return the title of the given URL from the given collection.
     * @param url URL to get title for
     * @param collectionConfig collection Config object
     * @return title or empty string if title is not available
     */
    public static String getTitle(String url, Config collectionConfig) {
        String title = "";
        List<String> urls = new ArrayList<>();
        urls.add(url);

        List<DocInfo> dis = getDocInfoResult(urls, collectionConfig).asList();

        if (dis != null && dis.size() == 1) {
            title = dis.get(0).getTitle();
        }

        return title;
    }
}