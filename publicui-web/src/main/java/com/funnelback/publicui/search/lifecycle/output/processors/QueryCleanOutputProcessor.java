package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
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
@Log4j
public class QueryCleanOutputProcessor extends AbstractOutputProcessor {

	private static final Pattern WEIGHTED_OPERATORS_PATTERN = Pattern.compile("\\^\\d+\\.\\d+");
	
	/**
	 * <p>List of what PADRE considers to be punctuation.</p>
	 * <p>We need those because PADRE will strip punctuation from 
	 * facet constraintes, eg "$++ trades & services $++" will become
	 * "$++ trades services $++".</p>
	 * 
	 * @see <tt>padre/src/index/extractor.c</tt>
	 */
	private static final String PADRE_PUNC_CHARS = "[\\Q.!?-@;,/:'\"/&()[]_|\\E]";
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction != null
				&& searchTransaction.hasQuestion() && SearchTransactionUtils.hasResultPacket(searchTransaction) 
				&& searchTransaction.getResponse().getResultPacket().getQuery() != null) {

			// Remove any weighted query operators
			String q = WEIGHTED_OPERATORS_PATTERN.matcher(
					searchTransaction.getResponse().getResultPacket().getQuery()
				).replaceAll("");
			
			// Remove any faceted navigation constraint
			for (String constraint: searchTransaction.getQuestion().getFacetsQueryConstraints()) {
				q = q.replaceAll("\\Q"+constraint+"\\E", "");
				
				// Also try with the PADRE striped version
				q = q.replaceAll("\\Q"+stripPunctuation(constraint)+"\\E", "");				
			}
			
			// Finally collapse all spaces
			q = q.replaceAll("\\s+", " ");
			
			log.debug("Cleaned query '" + searchTransaction.getResponse().getResultPacket().getQuery() + "' to '" + q.trim() + "'");
			searchTransaction.getResponse().getResultPacket().setQueryCleaned(q.trim());
			
		}
	}
	
	/**
	 * <p>Strip punctuation characters from a facet constraint.</p>
	 * 
	 * <p>The constraint has the form <tt>|X:"$++ ab, cd & ef $++"</tt>.
	 * Punctuation outside the quotes must be preserved, but replaced
	 * inside the quotes. Spaces must also be collapsed <strong>except</strong>
	 * the first and last one. The end result should be: <tt>|X:"$++ ab cd ef $++"</tt>.</p>
	 * 
	 * <p>Hopefully we can get rid of all this tomfoolery once FUN-3472
	 * is implemented in the Modern UI</p>
	 * 
	 * @param facetConstraint
	 */
	private String stripPunctuation(final String facetConstraint) {
		Pattern constraintPattern = Pattern.compile(
				"(.*?\\Q"+MetadataBasedCategory.INDEX_FIELD_BOUNDARY+"\\E\\s)"
				+ "(.*?)"
				+ "(\\s\\Q"+MetadataBasedCategory.INDEX_FIELD_BOUNDARY+"\\E.*)");
		Matcher m = constraintPattern.matcher(facetConstraint);
		if (m.find()) {
			// Remove punctuation
			String constraintStripped = m.group(1)
					+ m.group(2).replaceAll(PADRE_PUNC_CHARS, "")
					+ m.group(3);
			// Collapse spaces
			return constraintStripped.replaceAll("\\s+", " ");
		} else {
			return facetConstraint;
		}
	}
}
