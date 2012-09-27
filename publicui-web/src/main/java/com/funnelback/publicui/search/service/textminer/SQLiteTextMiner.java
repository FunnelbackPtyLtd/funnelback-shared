package com.funnelback.publicui.search.service.textminer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.utils.ObjectCache;
import com.funnelback.common.utils.SQLiteCache;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.TextMiner;

@Log4j
@Component
public class SQLiteTextMiner implements TextMiner {
	
	@Autowired
	private ConfigRepository configRepository;
	
	public EntityDefinition getEntityDefinition(String entity, Collection collection) {
		return getEntityDefinition(entity, collection, "noun-entities");
	}
	
	public EntityDefinition getCustomDefinition(String entity, Collection collection) {
		return getEntityDefinition(entity, collection, "custom");
	}

	/**
	 * Looks up an entity or its variant to the the JSON String
	 * @param entity
	 * @param config
	 * @param checkpointDir
	 * @param hashKey
	 * @return
	 */
	private String getEntityOrVariant(String entity, Config config, String hashKey) {
        File checkpointDir = new File(config.getCollectionRoot()
        		+ File.separator + DefaultValues.VIEW_LIVE
        		+ File.separator + DefaultValues.FOLDER_CHECKPOINT);
        
		String jsonString = null;
		File db = new File(checkpointDir, hashKey + ".sqlitedb");
		if (db.exists()) {
			ObjectCache cache = new SQLiteCache(db.getAbsolutePath(), config, false);
			ObjectCache variantCache = null;
			try {
				jsonString = (String) cache.get(entity);
				if (jsonString == null) {
                    // Try the variant cache
					db = new File(checkpointDir, hashKey + "_variants.sqlitedb");
					if (db.exists()) {
						variantCache = new SQLiteCache(db.getAbsolutePath(), config, false);
            			String originalVariant =  (String) variantCache.get(entity);
            			if (originalVariant != null) {
            				jsonString = (String) cache.get(originalVariant); 
            			}
					}
				}
			} finally {
				cache.close();
				if (variantCache != null) {
					variantCache.close();
				}
			}
		}
		
		return jsonString;
	}
	
	private String getNountEntity(String url, Config config, String hashKey) {
        File checkpointDir = new File(config.getCollectionRoot()
        		+ File.separator + DefaultValues.VIEW_LIVE
        		+ File.separator + DefaultValues.FOLDER_CHECKPOINT);

		File db = new File(checkpointDir, hashKey + ".sqlitedb");
		if (db.exists()) {
			ObjectCache cache = new SQLiteCache(db.getAbsolutePath(), config, false);
			try {
				return (String) cache.get(url);
			} finally {
				cache.close();
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public EntityDefinition getEntityDefinition(String entity, Collection collection, String hashName) {
		String hashKey = collection.getId() + TextMiner.TEXT_MINER_HASH + hashName;
        String jsonString = null;
        
		if (Type.meta.equals(collection.getType())) {
			String[] components = collection.getMetaComponents();

			// Loop over components
			for (String component : components) {
				Config componentConfig = configRepository.getCollection(component).getConfiguration();
				
				hashKey = component + TextMiner.TEXT_MINER_HASH + hashName;		            
				log.debug("Hash name: " + hashKey + " and field: " + entity);

				jsonString = getEntityOrVariant(entity, componentConfig, hashKey);
				
				if (jsonString != null) {
					break;
				}
			}
			
		} else {
            log.debug("Hash name: " + hashKey + " and field: " + entity);
            jsonString = getEntityOrVariant(entity, collection.getConfiguration(), hashKey);
		}	

		if (jsonString != null) {
			try {
				Map<String, String> element_map = new ObjectMapper().readValue(jsonString, Map.class);
				
				if (element_map != null) {
			        String definition = element_map.get("definition");
			        String source_url = element_map.get("sourceURL");
			        
			        return new EntityDefinition(entity, definition, source_url);
				}
			} catch (IOException ioe) {
				log.error("Could not unserialize JSON", ioe);
			}

		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<String> getURLNounPhrases(String url, Collection collection) {
		String hashName = "url-noun-entities";
	    
	    Config config = collection.getConfiguration();
        
		String hashKey = collection.getId() + TextMiner.TEXT_MINER_HASH + hashName;
        String jsonString = null;
        
		if (Type.meta.equals(collection.getType())) {

			String[] components = collection.getMetaComponents();

			for (String component : components) {
				Config componentConfig = configRepository.getCollection(component).getConfiguration();
		        
				hashKey = component + TextMiner.TEXT_MINER_HASH + hashName;		            
				log.debug("Hash name: " + hashKey + " and field: " + url);
				
				jsonString = getNountEntity(url, componentConfig, hashKey);
				
				if (jsonString != null) {
					break;
				}
			}
		}
		else {
            log.debug("Hash name: " + hashKey + " and field: " + url);
            jsonString = getNountEntity(url, config, hashKey);
		}
		
		if (jsonString != null) {
			try {
				return new ObjectMapper().readValue(jsonString, ArrayList.class);
			} catch (IOException ioe) {
				log.error("Could not unserialize JSON", ioe);
			}
		}
		
		return null;
	}
}
