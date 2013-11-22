package com.funnelback.publicui.recommender;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.utils.ObjectCache;
import com.funnelback.common.utils.SQLiteCache;
import com.funnelback.common.utils.StringCountComparator;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.publicui.recommender.dao.RecommenderDAO;
import com.funnelback.publicui.recommender.dataapi.DataAPI;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import com.funnelback.reporting.recommender.tuple.PreferenceTuple;
import com.funnelback.reporting.recommender.utils.RecommenderUtils;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides recommendations from the Recommender System for a given collection.
 *
 * @author fcrimmins@funnelback.com
 */
public class Recommender {
    private static final Logger logger = Logger.getLogger(Recommender.class);

    @Getter
    private Config collectionConfig;

    // Comparator for StringCount objects (based on their internal count value)
    public static final StringCountComparator FREQUENCY_ORDER = new StringCountComparator();

    private ConfigRepository configRepository;
    private DataAPI dataAPI;
    private RecommenderDAO recommenderDAO;

    /**
     * Create a Recommender for the given collection and seed item. The Recommender will try to determine if
     * the given seed item is present in the given collection or one of its components if it is a meta collection.
     * If it cannot find a collection with information on the given item then it will throw an
     *
     * @param collection       collection object
     * @param dataAPI          a handle to the Data API system
     * @param recommenderDAO   recommender data access object
     * @param seedItem         seed item (e.g. URL)
     * @param configRepository handle to configuration repository
     */
    public Recommender(com.funnelback.publicui.search.model.collection.Collection collection,
                       DataAPI dataAPI, RecommenderDAO recommenderDAO, String seedItem,
                       ConfigRepository configRepository) throws IllegalStateException {
        this.dataAPI = dataAPI;
        this.recommenderDAO = recommenderDAO;
        this.configRepository = configRepository;
        this.collectionConfig = getCollectionConfig(collection, seedItem);

        if (collectionConfig == null) {
            throw new IllegalStateException("Unable to create a valid collection config object for collection: "
                    + collection.getId() + " and seed item: " + seedItem);
        }
    }

    /**
     * Return a List of {@link com.funnelback.publicui.recommender.Recommendation}'s for the given item name.
     * Guarantees that the number of recommendations returned will never be greater than maxRecommendations
     * (unless maxRecommendations is < 1, which means 'unlimited').
     *
     * @param itemName           name of item
     * @param scope              comma separated list of items scopes
     * @param maxRecommendations maximum number of recommendations to display (less than 1 means unlimited)
     * @param source             expected source of recommendations (default is CLICKS)
     * @param searchService
     * @return List of recommendations (which may be empty).
     */
    public List<Recommendation> getRecommendationsForItem(String itemName, String scope,
                                                          int maxRecommendations, ItemTuple.Source source,
                                                          String searchService)
            throws IllegalStateException {
        List<Recommendation> recommendations = new ArrayList<>();
        List<ItemTuple> fullList;
        List<String> scopes = new ArrayList<>();

        if (scope != null && !("").equals(scope)) {
            scopes = Arrays.asList(scope.split(","));
        }

        fullList = recommenderDAO.getRecommendations(itemName, collectionConfig);

        if (fullList != null && fullList.size() > 0) {
            if (source.equals(ItemTuple.Source.RELATED_CLICKS) && fullList.size() < maxRecommendations) {
                List<ItemTuple> relatedClicks = RecommenderUtils.getRelatedClicks(itemName, fullList, collectionConfig);
                fullList.addAll(relatedClicks);
            }

            if (!("").equals(searchService)) {
                List<ItemTuple> relatedResults
                        = RecommenderUtils.getBlendedResults(itemName, fullList, collectionConfig, scope, searchService);
                fullList.addAll(relatedResults);
            }

            List<ItemTuple> scopedList = new ArrayList<>();

            for (ItemTuple item : fullList) {
                String itemValue = item.getItemID();

                if (itemValue != null && inScope(itemValue, scopes)) {
                    scopedList.add(item);
                }
            }

            if (scopedList.size() > 0) {
                List<String> indexURLs = new ArrayList<>();
                Map<String, ItemTuple> confidenceMap = new HashMap<>();

                for (ItemTuple item : scopedList) {
                    String indexURL = item.getItemID();
                    indexURLs.add(indexURL);
                    confidenceMap.put(indexURL, item);
                }

                recommendations = dataAPI.decorateURLRecommendations(indexURLs, confidenceMap, collectionConfig);

                if (recommendations != null && recommendations.size() > maxRecommendations) {
                    recommendations = recommendations.subList(0, maxRecommendations);
                }
            } else {
                logger.info("No items in scope from original list of size: " + fullList.size());
            }
        }

        return recommendations;
    }

    /**
     * Return true if the given item is considered "in scope" based on the given list
     * of scope patterns (which may be empty).
     *
     * @param item   String to test for display
     * @param scopes list of scope patterns e.g. cmis.csiro.au,-vic.cmis.csiro.au
     * @return true if item should be displayed
     */
    public boolean inScope(String item, List<String> scopes) {
        Pattern positiveRegex = null;
        Pattern negativeRegex = null;
        boolean inScope = false;

        if (scopes != null && scopes.size() > 0) {
            StringBuffer buf1 = new StringBuffer();
            StringBuffer buf2 = new StringBuffer();

            // First build up regular expressions
            for (String scopePattern : scopes) {
                if (scopePattern.startsWith(("-"))) {
                    // Negative scope pattern i.e. -handbook.curtin.edu.au
                    scopePattern = scopePattern.substring(1, scopePattern.length());
                    buf2.append(scopePattern + "|");
                } else {
                    buf1.append(scopePattern + "|");
                }
            }

            if (buf1.length() > 0) {
                // Remove trailing | before converting to String
                positiveRegex = Pattern.compile(buf1.deleteCharAt(buf1.length() - 1).toString());
            }

            if (buf2.length() > 0) {
                negativeRegex = Pattern.compile(buf2.deleteCharAt(buf2.length() - 1).toString());
            }

            if (negativeRegex != null) {
                Matcher negativeMatcher = negativeRegex.matcher(item);
                if (negativeMatcher.find()) {
                    // Negative match - return straight away
                    return false;
                }
            }

            // Didn't pass negative pattern - now check positive pattern (if it exists).
            if (positiveRegex != null) {
                Matcher positiveMatcher = positiveRegex.matcher(item);
                if (positiveMatcher.find()) {
                    return true;
                }
            } else {
                // Didn't match negative pattern and positive pattern was empty -> item is in scope
                inScope = true;
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
     * <p/>
     * For a meta collection (and each of its components) we first check if any session information is available
     * and if there is none we check if the Data API knows about the seed item. If neither source has information
     * we will move on to the next component until we have exhausted the list of components or found a component
     * that has information.
     *
     * @param collection Collection to derive collection configuration for
     * @param seedItem   the seed item to use in detecting which component to return
     * @return collection configuration (may be null).
     */
    private Config getCollectionConfig(com.funnelback.publicui.search.model.collection.Collection collection,
                                       String seedItem) {
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
                    logger.debug("Found session info for seed item: " + seedItem + " in component: " + component);
                    break;
                } else if (dataAPI != null) {
                    DocInfo docInfo = dataAPI.getDocInfo(seedItem, componentConfig);

                    if (docInfo != null) {
                        // No sessions found, but we do have information from the Data API
                        foundComponent = true;
                        logger.debug("Found Data API match info for seed item: " + seedItem
                                + " in component: " + component);
                        break;
                    }
                }
            }
        } else {
            componentConfig = collection.getConfiguration();

            if (seedItem != null && !("").equals(seedItem) && dataAPI != null) {
                DocInfo docInfo = dataAPI.getDocInfo(seedItem, componentConfig);

                if (docInfo != null) {
                    logger.debug("Found Data API match info for seed item: " + seedItem
                            + " in requested collection: " + componentConfig.getCollectionName());
                    foundComponent = true;
                }
            } else {
                // No given seed item - just return the configuration object for the given collection
                foundComponent = true;
            }
        }

        if (!foundComponent) {
            logger.debug("No matching component collection for seed item: " + seedItem);
            componentConfig = null;
        }

        return componentConfig;
    }

    public synchronized Set<List<PreferenceTuple>> getSessions(String itemName) {
        return getSessions(itemName, collectionConfig);
    }

    /**
     * Return a set of sessions that the given item appears in.
     *
     * @param itemName Name of item
     * @return set of sessions (which may be empty)
     */
    public synchronized Set<List<PreferenceTuple>> getSessions(String itemName, Config collectionConfig) {
        Set<List<PreferenceTuple>> sessions = new HashSet<>();
        String databaseFilename
                = SQLiteCache.getDatabaseFilename(collectionConfig, DefaultValues.VIEW_LIVE,
                com.funnelback.reporting.recommender.utils.RecommenderUtils.SESSIONS_HASH + DefaultValues.SQLITEDB);
        File db = new File(databaseFilename);
        String value;

        if (db.exists()) {
            ObjectCache database = new SQLiteCache(databaseFilename, collectionConfig, true);

            try {
                value = (String) database.get(itemName);

                if (value != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    // See http://wiki.fasterxml.com/JacksonInFiveMinutes#Data_Binding_with_Generics
                    Set<String> sessionIDs = mapper.readValue(value, new TypeReference<Set<String>>() {
                    });

                    if (sessionIDs != null && !sessionIDs.isEmpty()) {
                        for (String sessionID : sessionIDs) {
                            value = (String) database.get(sessionID);

                            if (value != null) {
                                List<PreferenceTuple> session
                                        = mapper.readValue(value, new TypeReference<List<PreferenceTuple>>() {
                                });

                                if (session != null) {
                                    sessions.add(session);
                                }
                            }
                        }
                    } else {
                        logger.warn("No sessions found for item: " + itemName);
                    }
                } else {
                    logger.warn("No value found in sessions cache for key: " + itemName);
                }
            } catch (Exception exception) {
                logger.warn("Exception getting value and converting from JSON: " + exception);
            } finally {
                database.close();
            }
        }

        return sessions;
    }
}
