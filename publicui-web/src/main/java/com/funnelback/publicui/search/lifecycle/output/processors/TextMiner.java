package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.List;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
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
	
	public static final String KEY_NOUN_PHRASES = "noun_phrases";
	public static final String KEY_CUSTOM_DEFINITION = "entity.custom-definition";

	@Autowired
	@Setter
	private com.funnelback.publicui.search.service.TextMiner textMiner;

	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)
				&& searchTransaction.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.TEXT_MINER)) {

			String query = searchTransaction.getQuestion().getQuery();
			log.debug("Received query: " + query);
			query = query.replaceAll("\"", "");
			query = WordUtils.capitalizeFully(query);

			if (searchTransaction.getQuestion().getCollection().getTextMinerBlacklist().contains(query.toLowerCase())) {
				log.debug("Blacklisted query: " + query);
			} else {
				generateDefinition(query, searchTransaction);
				generateNounPhrases(searchTransaction);
				generateCustomData(query, searchTransaction);
			}
		}
	}

	/**
	 * Generate a definition for the given query and insert into the data model.
	 */
	private void generateDefinition(String query, SearchTransaction searchTransaction) {
		Collection collection = searchTransaction.getQuestion().getCollection();

		long start_time = System.currentTimeMillis();
		EntityDefinition entityDefinition = textMiner.getEntityDefinition(query, collection);
		long total_time = (System.currentTimeMillis() - start_time);
		log.debug("Time to get entity and definition: " + total_time + "ms");

		if (entityDefinition != null) {
			searchTransaction.getResponse().setEntityDefinition(entityDefinition);
		}
	}

	/**
	 * Generate noun phrases for the result URLs and insert into the data model.
	 * 
	 */

	private void generateNounPhrases(SearchTransaction searchTransaction) {
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
			Collection collection = searchTransaction.getQuestion().getCollection();

			for (Result result : searchTransaction.getResponse().getResultPacket().getResults()) {
				String live_url = result.getLiveUrl();

				List<String> noun_phrase_list = textMiner.getURLNounPhrases(live_url, collection);

				if (noun_phrase_list != null) {
					result.getCustomData().put(KEY_NOUN_PHRASES, noun_phrase_list);
					log.debug("TextMiner: Inserted noun phrase data into data model: " + noun_phrase_list);
				}
			}
		}
	}

	/**
	 * Generate and custom data found in Redis and insert into the response.
	 * 
	 * @param query
	 *            seed query
	 * @param searchHome
	 *            value of SEARCH_HOME environment variable
	 */
	private void generateCustomData(String query, SearchTransaction searchTransaction) {
		Collection collection = searchTransaction.getQuestion().getCollection();
		EntityDefinition entityDefinition = textMiner.getCustomDefinition(query, collection);

		if (entityDefinition != null) {
			String definition = entityDefinition.getDefinition();
			searchTransaction.getResponse().getCustomData().put(KEY_CUSTOM_DEFINITION, definition);
		}
	}
}
