package com.funnelback.publicui.recommender.dao;

import com.funnelback.common.config.Config;
import com.funnelback.reporting.recommender.tuple.ItemTuple;

import java.util.List;

/**
 * This class defines an interface for a Recommender system DAO (Data Access Object).
 * @author fcrimmins@funnelback.com
 */
public interface RecommenderDAO {

    /**
     * Get a list of recommendations from for the given key (item ID) and collection.
     * @param key    key to lookup
     * @param collectionConfig collection configuration object
     * @return value as a list, which may be null
     */
    List<ItemTuple> getRecommendations(String key, Config collectionConfig) throws IllegalStateException;
}
