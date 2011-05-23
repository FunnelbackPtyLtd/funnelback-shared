package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;


/**
 * Helper to build a query string that will be passed to PADRE. 
 */
@RequiredArgsConstructor
public class PadreQueryStringBuilder {

	private static final String DELIMITER = "&";
	
	private final SearchTransaction transaction;

	/**
	 * Whether to apply faceted navigation constraints where
	 * building the query string or not
	 */
	private final boolean withFacetConstraints;
	
	public String buildQueryString() {
		Map<String, String[]> qs = new HashMap<String, String[]>();
		
		// Add any additional parameter
		qs.putAll(transaction.getQuestion().getAdditionalParameters());
		
		// Then craft our owns (Their value will overwrite any existing one in the additional set)
		qs.put(Parameters.collection.toString(), new String[] {transaction.getQuestion().getCollection().getId()});
		qs.put(Parameters.profile.toString(), new String[] {transaction.getQuestion().getProfile()});

		qs.put(Parameters.query.toString(), new String[] {buildQuery()});
		
		String gscope1 = buildGScope1();
		if (gscope1 != null && ! "".equals(gscope1)) {
			qs.put(Parameters.gscope1.toString(), new String[] {buildGScope1()});
		}
		
		// Remove from query string any parameter that will be passed as an environment variable
		for (String key : transaction.getQuestion().getEnvironmentVariables().keySet()) {
			qs.remove(key);
		}
		
		return toQueryString(qs);
		
	}
	
	@SneakyThrows(UnsupportedEncodingException.class)
	private static String toQueryString(Map<String, String[]> map) {
		StringBuffer out = new StringBuffer();
		for(Iterator<Entry<String, String[]>> it = map.entrySet().iterator(); it.hasNext();) {

			Entry<String, String[]> entry = it.next();
			for (int i=0;i<entry.getValue().length; i++) {
				out.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue()[i], "UTF-8"));
				if (i+1 < entry.getValue().length) {
					out.append(DELIMITER);
				}
			}
		
			if (it.hasNext()) {
				out.append(DELIMITER);
			}
		}
		return out.toString();
	}
	
	/**
	 * Builds query expression from the various question parameters
	 * (original query, <code>meta_</code> parameters, faceted navigation constraints, etc.)
	 * @param transaction
	 * @return
	 */
	public String buildQuery() {
		// Build query
		StringBuffer query = new StringBuffer(transaction.getQuestion().getQuery());
		if (transaction.getQuestion().getQueryExpressions().size() > 0) {
			// Add additional query expressions
			query.append(" " + StringUtils.join(transaction.getQuestion().getQueryExpressions(), " "));
		}
		
		if (transaction.getQuestion().getMetaParameters().size() > 0) {
			// Add meta_* parameters transformed as query expressions
			for (String value : transaction.getQuestion().getMetaParameters()) {
				query.append(" " + value);
			}
		}
		
		if (withFacetConstraints && transaction.getQuestion().getFacetsQueryConstraints().size() > 0) {
			// Add query constraints for faceted navigation
			for (String value: transaction.getQuestion().getFacetsQueryConstraints()) {
				query.append(" " + value);
			}
		}
		return query.toString();
	}
	
	/**
	 * Builds <code>gscope1</code> parameter using any existing input parameter combined
	 * with faceted navigation gscope constraints.
	 * @param transaction
	 * @return
	 */
	private String buildGScope1() {
		String facetGscopeConstraints = transaction.getQuestion().getFacetsGScopeConstraints();
		// Do we have gscope constraints due to faceted navigation ?
		if ( withFacetConstraints && facetGscopeConstraints!= null && ! "".equals(facetGscopeConstraints)) {
			
			// Do we have other gscope constraints (coming from query string)
			if (transaction.getQuestion().getAdditionalParameters().get(RequestParameters.GSCOPE1) != null) {
				// Combine them
				return transaction.getQuestion().getFacetsGScopeConstraints()
					// Only the [0] value is relevant
					+ (CharUtils.isAsciiNumeric(facetGscopeConstraints.charAt(facetGscopeConstraints.length()-1))
							? ","
							: "")
					+ transaction.getQuestion().getAdditionalParameters().get(RequestParameters.GSCOPE1)[0]
					+ "+";
			} else {
				return transaction.getQuestion().getFacetsGScopeConstraints();
			}
		} else {
			if (transaction.getQuestion().getAdditionalParameters().get(RequestParameters.GSCOPE1) != null) {
				return transaction.getQuestion().getAdditionalParameters().get(RequestParameters.GSCOPE1)[0];
			} else {
				return null;
			}
		}		
	}
	
	@RequiredArgsConstructor
	public static class ParameterValues {
		public final String parameter;
		public final String[] values;
	}
	
	public static enum Parameters {
		collection,query,profile,gscope1;
	}
	
}
