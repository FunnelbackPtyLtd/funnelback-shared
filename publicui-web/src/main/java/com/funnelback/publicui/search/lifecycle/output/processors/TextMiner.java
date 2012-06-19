package com.funnelback.publicui.search.lifecycle.output.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.WordUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.funnelback.common.Environment;
import com.funnelback.common.Security;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

@Component("textMinerOutputProcessor")
@Log4j
public class TextMiner implements OutputProcessor {
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
			String redisHost = searchTransaction.getQuestion().getCollection().getConfiguration().value("redis.master.host", "localhost");
			int redisPort = searchTransaction.getQuestion().getCollection().getConfiguration().valueAsInt("redis.master.port", 6379);
			String serverSecret = searchTransaction.getQuestion().getCollection().getConfiguration().value("server_secret");
			JedisPool pool = new JedisPool(new org.apache.commons.pool.impl.GenericObjectPool.Config(),
			        redisHost, redisPort, Protocol.DEFAULT_TIMEOUT,
			        Security.generateSystemPassword(Security.System.REDIS, serverSecret));

			Jedis jedis;

			if (serverSecret != null && !serverSecret.equals("")) {
			    jedis = pool.getResource();
			}
			else {
			    jedis = new Jedis("localhost");
			}
			boolean redisAvailable = false;

			try {
			    jedis.ping();
			    redisAvailable = true;
			}
			catch (JedisConnectionException jce) {
			    log.error("Problem connecting to Redis: " + jce);
			}

			if (redisAvailable) {
			    int MAX_PHRASES = 5;
			    File searchHome = Environment.getValidSearchHome();
			    String collection_id = searchTransaction.getQuestion().getCollection().getConfiguration().value("collection");
			    String collection_conf_dir = searchHome.toString() + File.separator + "conf" + File.separator + collection_id;

			    String query = searchTransaction.getQuestion().getQuery();
			    log.debug("Received query: " + query);
			    query = query.replaceAll("\"", "");
			    query = WordUtils.capitalizeFully(query);

			    // Process black list file if it exists
			    File black_list_file = new File(collection_conf_dir + File.separator + "text-miner-blacklist.cfg");
			    HashMap<String, String> black_list = new HashMap<String, String>();

			    try {
				    if (black_list_file.exists()) {
				        BufferedReader br = new BufferedReader(new FileReader(black_list_file));

				        String line;

				        while (br.ready()) {
				            line = br.readLine();

				            if (!line.startsWith("#")) {
				                line = line.toLowerCase();
				                black_list.put(line, "");
				            }
				        }

				        br.close();
				    }	
			    }
			    catch (Exception exception) {
			    	log.error(exception);
			    }

			    if (black_list.containsKey(query.toLowerCase())) {
			        log.debug("TextMiner: Blacklisted query: " + query);
			    }
			    else {
			        generateDefinition(jedis, query, collection_id, searchHome, searchTransaction);

			        generateNounPhrases(jedis, searchHome, MAX_PHRASES, searchTransaction);
			        generateCustomData(jedis, query, searchHome, searchTransaction);
			    }		
		   }
	
		}
	}

	/**
	 * Generate a definition for the given query and insert into the data model.
	 */
	@SuppressWarnings("unchecked")
	protected void generateDefinition(Jedis jedis, String query, String collection_id, File searchHome, SearchTransaction searchTransaction) {
	    Map<String, String> results = (Map<String, String>) queryRedis(jedis, "noun-entities", query, searchHome, searchTransaction);

	    if (results != null && !results.isEmpty()) {
	        String definition = results.get("definition");
	        String source_url = results.get("sourceURL");

	        log.debug("Definition: [" + definition + "] Source URL: " + source_url);

	        EntityDefinition entityDefinition = new EntityDefinition(query, definition, source_url);	        
	        searchTransaction.getResponse().setEntityDefinition(entityDefinition);
	    }
	    else {
	        log.debug("No definition found for query: " + query);
	    }
	}

	/**
	 * Generate noun phrases for the result URLs and insert into the data model.
	 */
	@SuppressWarnings("unchecked")
	protected void generateNounPhrases(Jedis jedis, File searchHome, int max_phrases, SearchTransaction searchTransaction) {
		for (Result result : searchTransaction.getResponse().getResultPacket().getResults()) {
	        String live_url = result.getLiveUrl();

	        List<String> noun_phrase_list = (List<String>) queryRedis(jedis, "url-noun-entities", live_url, searchHome, searchTransaction);

	        if (noun_phrase_list != null) {
		        result.getCustomData().put("noun_phrases", noun_phrase_list);
	            log.debug("TextMiner: Inserted noun phrase data into data model: " + noun_phrase_list);
	        }
	    }
	}

	/**
	 * Generate and custom data found in Redis and insert into the response.
	 * @param query seed query
	 * @param searchHome value of SEARCH_HOME environment variable
	 */
	@SuppressWarnings("unchecked")
	protected void generateCustomData(Jedis jedis, String query, File searchHome, SearchTransaction searchTransaction) {
	    Map<String, String> results = (Map<String, String>) queryRedis(jedis, "custom", query, searchHome, searchTransaction);

	    if (results != null && !results.isEmpty()) {
	        String definition = results.get("definition");

	        log.debug("Custom Definition: [" + definition + "]");

	        searchTransaction.getResponse().getCustomData().put("CUSTOM", definition);
	    }
	    else {
	        log.debug("No custom definition found for query: " + query);
	    }
	}

	/**
	 * Query redis for the given hashName and field. Will query for sub collection data
	 * if this is a meta collection.
	 * @param hashName the hashName to send to Redis
	 * @param field the field to send to Redis
	 * @return HashMap detailing the response from Redis. The map may be empty if no data could be got from Redis.
	 */
	@SuppressWarnings("unchecked")
	protected Object queryRedis(Jedis jedis, String hashName, String field, File searchHome, SearchTransaction searchTransaction) {
	    Map<String, String> element_map = null;
	    String collection_id = searchTransaction.getQuestion().getCollection().getConfiguration().value("collection");
	    String collection_type = searchTransaction.getQuestion().getCollection().getConfiguration().value("collection_type");
	    String redisKey = collection_id + ":text-miner:" + hashName;
	    ObjectMapper mapper = new ObjectMapper();
	    String jsonString;

	    try {
	        if (collection_type.equals("meta")) {
	            File meta_cfg = new File(searchHome.toString() + File.separator + "conf" + File.separator
	                    + collection_id + File.separator + "meta.cfg");

	            if (meta_cfg.exists()) {
	                BufferedReader br = new BufferedReader(new FileReader(meta_cfg));

	                String line;

	                while (br.ready()) {
	                    line = br.readLine();

	                    if (!line.startsWith("#")) {
	                        collection_id = line;

	                        redisKey = collection_id + ":text-miner:" + hashName;
	                        log.debug("Hash name: " + redisKey + " and field: " + field);

	                        jsonString = jedis.hget(redisKey, field);

	                        if (jsonString == null && !hashName.equals("url-noun-entities")) {
	                            // Try the variant cache
	                            String variantKey = redisKey + ":variants";
	                            String originalVariant = jedis.hget(variantKey, field);

	                            if (originalVariant != null) {
	                                jsonString = jedis.hget(redisKey, originalVariant);

	                                if (jsonString != null) {
	                                    element_map = mapper.readValue(jsonString, Map.class);
	                                }
	                                break;
	                            }
	                        }
	                        else if (jsonString != null && hashName.equals("url-noun-entities")) {
	                            ArrayList<String> url_noun_list = mapper.readValue(jsonString, ArrayList.class);
	                            return url_noun_list;
	                        }
	                        else if (jsonString != null) {
	                            element_map = mapper.readValue(jsonString, Map.class);
	                            // Use the first result set we get
	                            break;
	                        }
	                    }
	                }

	                br.close();
	            }
	        }
	        else {
	            log.debug("Hash name: " + redisKey + " and field: " + field);
	            jsonString = jedis.hget(redisKey, field);

	            if (jsonString == null && !hashName.equals("url-noun-entities")) {
	                // Try the variant cache
	                String variantKey = redisKey + ":variants";
	                String originalVariant = jedis.hget(variantKey, field);

	                if (originalVariant != null) {
	                    jsonString = jedis.hget(redisKey, originalVariant);
	                    element_map = mapper.readValue(jsonString, Map.class);
	                }
	            }
	            else if (hashName.equals("url-noun-entities")) {
	                ArrayList<String> url_noun_list = mapper.readValue(jsonString, ArrayList.class);
	                return url_noun_list;
	            }
	            else {
	                element_map = mapper.readValue(jsonString, Map.class);
	            }
	        }
	    }
	    catch (JedisConnectionException jce) {
	        log.error("Unable to connect to Redis: " + jce);
	    }
	    catch (Throwable exception) {
	        log.debug("queryRedis: " + exception);
	    }

	    return element_map;
	}
}
