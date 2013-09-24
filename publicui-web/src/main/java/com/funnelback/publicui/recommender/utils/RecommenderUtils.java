package com.funnelback.publicui.recommender.utils;

import com.funnelback.common.config.*;
import com.funnelback.common.utils.ObjectCache;
import com.funnelback.common.utils.SQLiteCache;
import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.search.service.ConfigRepository;
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
     * Return a List of {@link com.funnelback.publicui.recommender.Recommendation}'s for the given item name.
     * @param itemName           name of item
     * @param collectionConfig   collection config object
     * @param scope              comma separated list of items scopes
     * @param maxRecommendations maximum number of recommendations to display (less than 1 means unlimited)
     * @return List of recommendations (which may be empty).
     */
    public static List<Recommendation> getRecommendationsForItem(String itemName, Config collectionConfig,
                                                                 String scope, int maxRecommendations) throws IllegalStateException {
        List<Recommendation> recommendations = new ArrayList<>();
        List<ItemTuple> fullList;
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

            List<String> urls = new ArrayList<>();
            Map<String, ItemTuple> confidenceMap = new HashMap<>();

            for (ItemTuple item : scopedList) {
                String url = item.getItemID();
                urls.add(url);
                confidenceMap.put(url, item);
            }

            recommendations = decorateURLRecommendations(urls, confidenceMap, collectionConfig);
        }

        return recommendations;
    }

    /**
     * Return a list of URL recommendations which have been "decorated" with information from the Data API/libi4u.
     * @param urls list of URL strings to decorate
     * @param confidenceMap Optional map of urls to confidence scores (can be null if not available).
     * @param collectionConfig collection config object
     * @return list of decorated URL recommendations (which may be empty)
     */
    public static List<Recommendation> decorateURLRecommendations(List<String> urls,
            Map<String, ItemTuple> confidenceMap, Config collectionConfig) {
        List<Recommendation> recommendations = new ArrayList<>();
        List<DocInfo> dis = getDocInfoResult(urls, collectionConfig).asList();
        float confidence = -1;

        if (dis != null) {
            for (DocInfo docInfo : dis) {
                URI uri = docInfo.getUri();
                String itemID = uri.toString();

                if (confidenceMap != null) {
                    confidence = confidenceMap.get(itemID).getScore();
                }

                Recommendation recommendation = new Recommendation(itemID, confidence, docInfo);
                recommendations.add(recommendation);
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
     * Return the appropriate collection configuration for this collection. If the collection is a meta collection
     * then its own configuration may be returned or one of its components, depending on which component has
     * information on the given seed item. If this is not a meta collection then the configuration for the
     * specified collection will always be returned.
     *
     * For a meta collection (and each of its components) we first check if any session information is available
     * and if there is none we check if the Data API knows about the seed item. If neither source has information
     * we will move on to the next component until we have exhausted the list of components or found a component
     * that has information.
     * @param collection Collection to derive collection configuration for
     * @param configRepository handle to configuration repository
     * @param seedItem the seed item to use in detecting which component to return
     * @return collection configuration (may be null).
     */
    public static Config getCollectionConfig(com.funnelback.publicui.search.model.collection.Collection collection,
                                             ConfigRepository configRepository, String seedItem) {
        Config componentConfig = null;
        boolean foundComponent = false;

        if (com.funnelback.common.config.Collection.Type.meta.equals(collection.getType())) {
            List<String> components = new ArrayList<>();
            // Make sure we add the meta collection itself as the first element of the list to be checked.
            components.add(collection.getId());
            components.addAll(Arrays.asList(collection.getMetaComponents()));

            // Loop over all components (with parent first), until we find a component that knows about the item
            for (String component : components) {
                componentConfig = configRepository.getCollection(component).getConfiguration();
                Set<List<PreferenceTuple>> sessions = getSessions(seedItem, componentConfig);

                if (sessions != null && sessions.size() > 0) {
                    // This collection has session information on the seed item - use it.
                    foundComponent = true;
                    break;
                }
                else {
                    DocInfo docInfo = RecommenderUtils.getDocInfo(seedItem, componentConfig);

                    if (docInfo != null) {
                        // No sessions found, but we do have information from the Data API
                        foundComponent = true;
                        break;
                    }
                }
            }

            if (!foundComponent) {
                componentConfig = null;
            }
        }
        else {
            componentConfig = collection.getConfiguration();
        }

        return componentConfig;
    }

    /**
     * Get a list of recommendations from a backing cache based on the given Config and hashName.
     *
     * @param key    key to lookup in cache
     * @param config collection config object
     * @return value as a list, which may be null
     */
    public static List<ItemTuple> getRecommendationsFromCache(String key, Config config) throws IllegalStateException {
        List<ItemTuple> items = null;
        String baseName = com.funnelback.reporting.recommender.utils.RecommenderUtils.DATA_MODEL_HASH
                + DefaultValues.SQLITEDB;
        String databaseFilename
                = SQLiteCache.getDatabaseFilename(config, DefaultValues.VIEW_LIVE, baseName);
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
        else {
            String collectionName = config.getCollectionName();
            throw new IllegalStateException("Expected database file does not exist: " + collectionName + baseName
                    + " for collection " + collectionName);
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
                com.funnelback.reporting.recommender.utils.RecommenderUtils.SESSIONS_HASH + DefaultValues.SQLITEDB);
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
     * Return a DocInfo object for a single URL.
     * @param url URL string to get DocInfo for
     * @param collectionConfig collection config object
     * @return DocInfo object (may be null if unable to get information)
     */
    public static DocInfo getDocInfo(String url, Config collectionConfig) {
        DocInfo docInfo = null;

        List<String> urls = new ArrayList<>();
        urls.add(url);

        List<DocInfo> dis = getDocInfoResult(urls, collectionConfig).asList();

        if (dis != null && dis.size() == 1) {
            docInfo = dis.get(0);
        }

        return docInfo;
    }

    /**
     * Return the title of the given URL from the given collection.
     * @param url URL to get title for
     * @param collectionConfig collection Config object
     * @return title or empty string if title is not available
     */
    public static String getTitle(String url, Config collectionConfig) {
        String title = "";
        DocInfo docInfo = getDocInfo(url, collectionConfig);

        if (docInfo != null ) {
            title = docInfo.getTitle();
        }

        return title;
    }
}
