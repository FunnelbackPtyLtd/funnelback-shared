package com.funnelback.publicui.search.service.textminer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import com.funnelback.common.Security;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;
import com.funnelback.publicui.search.service.TextMiner;

@Log4j
@Component
public class RedisTextMiner implements TextMiner {

	private JedisPool getPool(Collection collection) {
		JedisPool pool = null;

		String redisHost = collection.getConfiguration().value("redis.master.host", "localhost");
		int redisPort = collection.getConfiguration().valueAsInt("redis.master.port", 6379);
		String serverSecret = collection.getConfiguration().value("server_secret");
		pool = new JedisPool(new org.apache.commons.pool.impl.GenericObjectPool.Config(),
				redisHost, redisPort, Protocol.DEFAULT_TIMEOUT,
				Security.generateSystemPassword(Security.System.REDIS, serverSecret));

		return pool;
	}

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

		JedisPool pool = getPool(collection);
		Jedis jedis = pool.getResource();

		String collection_id = collection.getConfiguration().value("collection");
		String collection_type = collection.getConfiguration().value("collection_type");

		String redisKey = collection_id + ":text-miner:" + hashName;
        String jsonString = "";
        
		try {
			if (collection_type.equals("meta")) {
				String[] components = collection.getMetaComponents();

				for (String component : components) {
					redisKey = component + ":text-miner:" + hashName;		            
					log.debug("Hash name: " + redisKey + " and field: " + entity);

					jsonString = jedis.hget(redisKey, entity);
					
	                if (jsonString == null) {
	                    // Try the variant cache
	                    String variantKey = redisKey + ":variants";
	                    String originalVariant = jedis.hget(variantKey, entity);

	                    if (originalVariant != null) {
	                        jsonString = jedis.hget(redisKey, originalVariant);

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
	            log.debug("Hash name: " + redisKey + " and field: " + entity);
	            jsonString = jedis.hget(redisKey, entity);

	            if (jsonString == null) {
	                // Try the variant cache
	                String variantKey = redisKey + ":variants";
	                String originalVariant = jedis.hget(variantKey, entity);

	                if (originalVariant != null) {
	                    jsonString = jedis.hget(redisKey, originalVariant);
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
			pool.returnResource(jedis);
			pool.destroy();
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
	    
		JedisPool pool = getPool(collection);
		Jedis jedis = pool.getResource();

		String collection_id = collection.getConfiguration().value("collection");
		String collection_type = collection.getConfiguration().value("collection_type");

		String redisKey = collection_id + ":text-miner:" + hashName;
        String jsonString = "";
        
		try {
			if (collection_type.equals("meta")) {
				String[] components = collection.getMetaComponents();

				for (String component : components) {
					redisKey = component + ":text-miner:" + hashName;		            
					log.debug("Hash name: " + redisKey + " and field: " + URL);

					jsonString = jedis.hget(redisKey, URL);
					
					if (jsonString != null) {
                        nounPhrases = mapper.readValue(jsonString, ArrayList.class);
                        break;
                    }
				}
			}
			else {
	            log.debug("Hash name: " + redisKey + " and field: " + URL);
	            jsonString = jedis.hget(redisKey, URL);

				if (jsonString != null) {
                    nounPhrases = mapper.readValue(jsonString, ArrayList.class);
                }
			}	
		}
		catch (Exception exception) {
			log.error(exception);
		}
		finally {
			pool.returnResource(jedis);
			pool.destroy();
		}
		
		return nounPhrases;
	}
}
