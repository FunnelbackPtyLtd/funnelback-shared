package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Cleans query by removing "technical" operators that we want
 * to hide to the user. Typically:
 * <ul>
 * 	<li>Weighted query terns: term^1.234 => term</li>
 *  <li>Faceted navigation query cooking</li>
 * </ul>
 *
 */
@Component("queryCleanOutputProcessor")
@Log
public class QueryCleanOutputProcessor implements OutputProcessor {

	private static final Pattern WEIGHTED_OPERATORS_PATTERN = Pattern.compile("\\^\\d+\\.\\d+");
	
	/**
	 * <p>List of what PADRE considers to be punctuation.</p>
	 * <p>We need those because PADRE will strip punctuation from 
	 * facet constraintes, eg "$++ trades & services $++" will become
	 * "$++ trades services $++".</p>
	 * 
	 * @see <tt>padre/src/index/extractor.c</tt>
	 */
	private static final String PADRE_PUNC_CHARS = "\\s[\\Q.!?-@;,/:'\"/&()[]_|\\E]\\s";
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction.hasQuestion() && SearchTransactionUtils.hasResultPacket(searchTransaction) 
				&& searchTransaction.getResponse().getResultPacket().getQuery() != null) {

			// Remove any weighted query operators
			String q = WEIGHTED_OPERATORS_PATTERN.matcher(
					searchTransaction.getResponse().getResultPacket().getQuery()
				).replaceAll("");
			
			// Remove any faceted navigation constraint
			for (String constraint: searchTransaction.getQuestion().getFacetsQueryConstraints()) {
				// Try both with the original constraint, and the
				// PADRE stripped version
				q = q.replaceAll("\\Q"+constraint+"\\E", "")
					.replaceAll("\\Q"+constraint.replaceAll(PADRE_PUNC_CHARS, " ")+"\\E", "");
			}
			
			log.debug("Cleaned query '" + searchTransaction.getResponse().getResultPacket().getQuery() + "' to '" + q.trim() + "'");
			searchTransaction.getResponse().getResultPacket().setQueryCleaned(q.trim());
			
		}
	}
}
