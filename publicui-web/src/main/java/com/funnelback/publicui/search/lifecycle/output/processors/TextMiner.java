package com.funnelback.publicui.search.lifecycle.output.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.Environment;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

@Component("textMinerOutputProcessor")
@Log4j
public class TextMiner implements OutputProcessor {
	
	@Autowired
	private com.funnelback.publicui.search.service.TextMiner textMiner;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
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
			        generateDefinition(query, searchTransaction);
			        generateNounPhrases(searchTransaction);
			        generateCustomData(query, searchTransaction);
			    }		
		   }
	}

	/**
	 * Generate a definition for the given query and insert into the data model.
	 */
	protected void generateDefinition(String query, SearchTransaction searchTransaction) {
	    Collection collection = searchTransaction.getQuestion().getCollection();
	    EntityDefinition entityDefinition = textMiner.getEntityDefinition(query, collection);

	    if (entityDefinition != null) {
		    searchTransaction.getResponse().setEntityDefinition(entityDefinition);	    	
	    }
	}

	/**
	 * Generate noun phrases for the result URLs and insert into the data model.
	 *
	 */
	
	protected void generateNounPhrases(SearchTransaction searchTransaction) {
	    Collection collection = searchTransaction.getQuestion().getCollection();

		for (Result result : searchTransaction.getResponse().getResultPacket().getResults()) {
	        String live_url = result.getLiveUrl();

	        List<String> noun_phrase_list = textMiner.getURLNounPhrases(live_url, collection);

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
	protected void generateCustomData(String query, SearchTransaction searchTransaction) {
	    Collection collection = searchTransaction.getQuestion().getCollection();
	    EntityDefinition entityDefinition = textMiner.getCustomDefinition(query, collection);

	    if (entityDefinition != null) {
	    	String definition = entityDefinition.getDefinition();
	        searchTransaction.getResponse().getCustomData().put("CUSTOM", definition);   
	    }
	}
}
