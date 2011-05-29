package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

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
		if (searchTransaction.hasQuestion() && searchTransaction.hasResponse() 
				&& searchTransaction.getResponse().getResultPacket().getQuery() != null) {

			// Remove any weighted query operators
			String q = WEIGHTED_OPERATORS_PATTERN.matcher(
					searchTransaction.getResponse().getResultPacket().getQuery()
				).replaceAll("");
			
			// Remove any faceted navigation constraint
			for (String constraint: searchTransaction.getQuestion().getFacetsQueryConstraints()) {
				q = q.replaceAll("\\Q"+constraint+"\\E", "");
			}
			
			log.debug("Cleaned query '" + searchTransaction.getResponse().getResultPacket().getQuery() + "' to '" + q.trim() + "'");
			searchTransaction.getResponse().getResultPacket().setQueryCleaned(q.trim());
			
		}
	}
}
