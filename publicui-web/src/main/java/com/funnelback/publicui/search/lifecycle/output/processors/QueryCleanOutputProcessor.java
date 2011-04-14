package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

/**
 * Cleans query by removing "technical" operators that we want
 * to hide to the user. Typically:
 * <ul>
 * 	<li>Weighted query terns: term^1.234 => term</li>
 *  <li>Faceted navigation query cooking</li>
 * </ul>
 * @author Administrator
 *
 */
@Component("queryCleanOutputProcessor")
@Log
public class QueryCleanOutputProcessor implements OutputProcessor {

	private static final Pattern WEIGHTED_OPERATORS_PATTERN = Pattern.compile("\\^\\d+\\.\\d+");
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction.hasResponse() && SearchTransactionUtils.hasResults(searchTransaction)) {

			// Remove any weighted query operators
			String q = WEIGHTED_OPERATORS_PATTERN.matcher(
					searchTransaction.getResponse().getResultPacket().getQuery()
				).replaceAll("");
			
			if (SearchTransactionUtils.hasCollection(searchTransaction)) {
				FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
				
				if (config != null) {
					// Remove each query expression related to a facet config
					for(String field: config.getMetadataFieldsUsed()) {
						// delete phrases e.g. x:"a phrase"
						q = q.replaceAll("|" + field + ":\".+?\"\\s?", "");
						
						// delete single elements e.g. x:query
						q = q.replaceAll("|" + field + ":[^\"]\\S*\\s?", "");
					}
				}
			}
			
			log.debug("Cleaned query '" + searchTransaction.getResponse().getResultPacket().getQuery() + "' to '" + q + "'");
			searchTransaction.getResponse().getResultPacket().setQueryCleaned(q);
			
		}
	}
}
