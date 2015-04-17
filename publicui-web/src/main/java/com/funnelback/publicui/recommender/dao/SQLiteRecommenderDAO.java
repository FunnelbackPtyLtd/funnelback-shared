package com.funnelback.publicui.recommender.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnelback.common.views.View;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.cache.ObjectCache;
import com.funnelback.common.cache.SQLiteCache;
import com.funnelback.reporting.recommender.tuple.ItemTuple;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;

/**
 * This class implements the Recommender system DAO (Data Access Object) interface. It uses the SQLite
 * database to access cached recommendation information.
 * @author fcrimmins@funnelback.com
 */

@Component
@Log4j2
public class SQLiteRecommenderDAO implements RecommenderDAO {

    /**
     * Get a list of recommendations for the given key (item ID) and collection.
     * @param key    key to lookup
     * @param collectionConfig collection configuration object
     * @return value as a list, which may be empty (but not null)
     */
    public List<ItemTuple> getRecommendations(String key, Config collectionConfig) throws IllegalStateException {
        List<ItemTuple> items = null;
        String baseName = com.funnelback.reporting.recommender.utils.RecommenderUtils.DATA_MODEL_HASH
                + DefaultValues.SQLITEDB;
        String databaseFilename
                = SQLiteCache.getDatabaseFilename(collectionConfig, View.live, baseName);
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
                    log.warn("No value found in recommendations cache for key: " + key);
                }
            } catch (Exception exception) {
                log.warn("Exception getting value and converting from JSON: " + exception);
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

        if (items == null) {
            items = new ArrayList<>();
        }

        return items;
    }
}
