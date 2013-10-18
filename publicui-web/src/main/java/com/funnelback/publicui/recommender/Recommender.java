package com.funnelback.publicui.recommender;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.utils.ObjectCache;
import com.funnelback.common.utils.SQLiteCache;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.publicui.recommender.dataapi.DataAPI;
import com.funnelback.publicui.recommender.dataapi.DataAPIConnectorPADRE;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import com.funnelback.reporting.recommender.tuple.PreferenceTuple;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;

import lombok.Getter;

/**
 * This class provides recommendations from the Recommender System for a given collection.
 * @author fcrimmins@funnelback.com
 */
public class Recommender {
    private static final Logger logger = Logger.getLogger(Recommender.class);

    @Getter
    private Config collectionConfig;
    
    @Autowired
    private ConfigRepository configRepository;
    
    private DataAPI dataAPI;
    
    /**
     * Create a Recommender for the given collection.
     * @param collection collection object
     * @param dataAPI a handle to the Data API system
     */
    public Recommender(com.funnelback.publicui.search.model.collection.Collection collection, DataAPI dataAPI) {
        this(collection, dataAPI, "");	
    }
    
    /**
     * Create a Recommender for the given collection and seed item. The Recommender will try to determine if
     * the given seed item is present in the given collection or one of its components if it is a meta collection.
     * If it cannot find a collection with information on the given item then it will throw an 
     * @param collection collection object
     * @param dataAPI a handle to the Data API system
     * @param seedItem seed item (e.g. URL)
     */
    public Recommender(com.funnelback.publicui.search.model.collection.Collection collection, 
    		DataAPI dataAPI, String seedItem) throws IllegalStateException	{
        this.dataAPI = dataAPI;
    	this.collectionConfig = getCollectionConfig(collection, seedItem);
        
        if (collectionConfig == null) {
        	throw new IllegalStateException("Unable to create a valid collection config object for collection: "
        			+ collection.getId() + " and seed item: " + seedItem);
        }       
    }
    
    /**
     * Return a List of {@link com.funnelback.publicui.recommender.Recommendation}'s for the given item name.
     *
     * @param itemName           name of item
     * @param scope              comma separated list of items scopes
     * @param maxRecommendations maximum number of recommendations to display (less than 1 means unlimited)
     * @return List of recommendations (which may be empty).
     */
    public List<Recommendation> getRecommendationsForItem(String itemName, String scope, int maxRecommendations) 
    		throws IllegalStateException {
        List<Recommendation> recommendations = new ArrayList<>();
        List<ItemTuple> fullList;
        List<String> scopes = new ArrayList<>();

        if (scope != null && !("").equals(scope)) {
            scopes = Arrays.asList(scope.split(","));
        }

        fullList = getRecommendationsFromCache(itemName);

        if (fullList != null && fullList.size() > 0) {
            List<ItemTuple> scopedList = new ArrayList<>();

            for (ItemTuple item : fullList) {
                String itemValue = item.getItemID();

                if (itemValue != null && inScope(itemValue, scopes)) {
                    scopedList.add(item);
                }
            }

            List<String> indexURLs = new ArrayList<>();
            Map<String, ItemTuple> confidenceMap = new HashMap<>();

            for (ItemTuple item : scopedList) {
                String indexURL = item.getItemID();
                indexURLs.add(indexURL);
                confidenceMap.put(indexURL, item);
            }

            recommendations = dataAPI.decorateURLRecommendations(indexURLs, confidenceMap, collectionConfig, maxRecommendations);
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
     * <p/>
     * For a meta collection (and each of its components) we first check if any session information is available
     * and if there is none we check if the Data API knows about the seed item. If neither source has information
     * we will move on to the next component until we have exhausted the list of components or found a component
     * that has information.
     *
     * @param collection       Collection to derive collection configuration for
     * @param seedItem         the seed item to use in detecting which component to return
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
                Set<List<PreferenceTuple>> sessions = getSessions(seedItem);

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
            }
            else {
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

    /**
     * Get a list of recommendations from a backing cache based on the given Config and hashName.
     *
     * @param key    key to lookup in cache
     * @return value as a list, which may be null
     */
    public List<ItemTuple> getRecommendationsFromCache(String key) throws IllegalStateException {
        List<ItemTuple> items = null;
        String baseName = com.funnelback.reporting.recommender.utils.RecommenderUtils.DATA_MODEL_HASH
                + DefaultValues.SQLITEDB;
        String databaseFilename
                = SQLiteCache.getDatabaseFilename(collectionConfig, DefaultValues.VIEW_LIVE, baseName);
        File db = new File(databaseFilename);
        String value;

        if (db.exists()) {
            ObjectCache database = new SQLiteCache(databaseFilename, collectionConfig, true);

            try {
                value = (String) database.get(key);

                if (value != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    // See http://wiki.fasterxml.com/JacksonInFiveMinutes#Data_Binding_with_Generics
                    items = mapper.readValue(value, new TypeReference<List<ItemTuple>>() {});
                } else {
                    logger.warn("No value found in recommendations cache for key: " + key);
                }
            } catch (Exception exception) {
                logger.warn("Exception getting value and converting from JSON: " + exception);
            } finally {
            	if (database != null) {
                    database.close();            		
            	}
            }
        } else {
            String collectionName = collectionConfig.getCollectionName();
            throw new IllegalStateException("Expected database file does not exist: " + collectionName + baseName
                    + " for collection " + collectionName);
        }

        return items;
    }

    /**
     * Return a set of sessions that the given item appears in.
     *
     * @param itemName Name of item
     * @return set of sessions (which may be empty)
     */
    public synchronized Set<List<PreferenceTuple>> getSessions(String itemName) {
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
