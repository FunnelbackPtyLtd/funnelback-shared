package com.funnelback.publicui.search.service.textminer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.utils.ObjectCache;
import com.funnelback.common.utils.SQLiteCache;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;
import com.funnelback.publicui.search.service.TextMiner;

@Log4j
@Component
public class SQLiteTextMiner implements TextMiner {
	public EntityDefinition getEntityDefinition(String entity, Collection collection) {
		return getEntityDefinition(entity, collection, "noun-entities");
	}
	
	public EntityDefinition getCustomDefinition(String entity, Collection collection) {
		return getEntityDefinition(entity, collection, "custom");
	}
	
	@SuppressWarnings("unchecked")
	public EntityDefinition getEntityDefinition(String entity, Collection collection, String hashName) {
		EntityDefinition entityDefinition = null;
	    Map<String, String> element_map = null;
	    ObjectMapper mapper = new ObjectMapper();
	    ObjectCache cache = null;
	    
	    Config config = collection.getConfiguration();
        File checkpointDir = new File(collection.getConfiguration().getCollectionRoot()
        		+ File.separator + DefaultValues.VIEW_LIVE
        		+ File.separator + DefaultValues.FOLDER_CHECKPOINT);
        
		String hashKey = collection.getId() + TextMiner.TEXT_MINER_HASH + hashName;
        String jsonString = "";
        
		try {
			if (Type.meta.equals(collection.getType())) {
				String[] components = collection.getMetaComponents();

				for (String component : components) {
					hashKey = component + TextMiner.TEXT_MINER_HASH + hashName;		            
					log.debug("Hash name: " + hashKey + " and field: " + entity);

		            cache = new SQLiteCache(new File(checkpointDir, hashKey + ".sqlitedb").getAbsolutePath(), config, false);

					jsonString = (String) cache.get(entity);
					
	                if (jsonString == null) {
	                    // Try the variant cache
	                    String variantKey = hashKey + ":variants";
	                    cache.close();
			            cache = new SQLiteCache(new File(checkpointDir, variantKey + ".sqlitedb").getAbsolutePath(), config, false);

	                    String originalVariant = (String) cache.get(entity);

	                    if (originalVariant != null) {
	                        jsonString = (String) cache.get(originalVariant);

	                        if (jsonString != null) {
	                            element_map = mapper.readValue(jsonString, Map.class);
	                        }
	                        break;
	                    }
	                }
                    else {
                        element_map = mapper.readValue(jsonString, Map.class);
                        // Use the first result set we get
                        break;
                    }
				}
			}
			else {
	            log.debug("Hash name: " + hashKey + " and field: " + entity);
	            cache = new SQLiteCache(checkpointDir + File.separator + hashKey + ".sqlitedb", config, false);
	            jsonString = (String) cache.get(entity);

	            if (jsonString == null) {
	                // Try the variant cache
	                String variantKey = hashKey + ":variants";
                    cache.close();
    	            cache = new SQLiteCache(checkpointDir + File.separator + variantKey + ".sqlitedb", config, false);
                    
	                String originalVariant =  (String) cache.get(entity);

	                if (originalVariant != null) {
	                    jsonString = (String) cache.get(originalVariant);
	                    element_map = mapper.readValue(jsonString, Map.class);
	                }
	            }
	            else {
	                element_map = mapper.readValue(jsonString, Map.class);
	            }
			}	
		}
		catch (Exception exception) {
			log.error(exception);
		}
		finally {
			if (cache != null) {
				cache.close();
			}
		}

		if (element_map != null) {
	        String definition = element_map.get("definition");
	        String source_url = element_map.get("sourceURL");
	        
	        entityDefinition = new EntityDefinition(entity, definition, source_url);
		}
		
		return entityDefinition;
	}

	@SuppressWarnings("unchecked")
	public List<String> getURLNounPhrases(String URL, Collection collection) {
		List<String> nounPhrases = null;
		String hashName = "url-noun-entities";
	    ObjectMapper mapper = new ObjectMapper();
	    ObjectCache cache = null;
	    
	    Config config = collection.getConfiguration();
        File checkpointDir = new File(collection.getConfiguration().getCollectionRoot()
        		+ File.separator + DefaultValues.VIEW_LIVE
        		+ File.separator + DefaultValues.FOLDER_CHECKPOINT);
        
		String hashKey = collection.getId() + TextMiner.TEXT_MINER_HASH + hashName;
        String jsonString = "";
        
		try {
			if (Type.meta.equals(collection.getType())) {
				String[] components = collection.getMetaComponents();

				for (String component : components) {
					hashKey = component + TextMiner.TEXT_MINER_HASH + hashName;		            
					log.debug("Hash name: " + hashKey + " and field: " + URL);

		            cache = new SQLiteCache(new File(checkpointDir, hashKey + ".sqlitedb").getAbsolutePath(), config, false);
		            jsonString = (String) cache.get(URL);
					
					if (jsonString != null) {
                        nounPhrases = mapper.readValue(jsonString, ArrayList.class);
                        break;
                    }					
				}
			}
			else {
	            log.debug("Hash name: " + hashKey + " and field: " + URL);
	            cache = new SQLiteCache(new File(checkpointDir, hashKey + ".sqlitedb").getAbsolutePath(), config, false);
	            jsonString = (String) cache.get(URL);
	            
				if (jsonString != null) {
                    nounPhrases = mapper.readValue(jsonString, ArrayList.class);
                }
			}	
		}
		catch (Exception exception) {
			log.error(exception);
		}
		finally {
			if (cache != null) {
				cache.close();
			}
		}
		
		return nounPhrases;
	}
}
