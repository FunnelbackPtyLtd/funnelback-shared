package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;


/**
 * Helper to build a query string that will be passed to PADRE. 
 */
@RequiredArgsConstructor
public class PadreQueryStringBuilder {

	private static final String DELIMITER = "&";
	
	private final SearchQuestion question;

	/**
	 * Whether to apply faceted navigation constraints where
	 * building the query string or not
	 */
	private final boolean withFacetConstraints;
	
	public String buildQueryString() {
		Map<String, String> qs = new TreeMap<String, String>();
		
		// Add any additional parameter
		qs.putAll(question.getAdditionalParameters());
		
		// Then craft our owns (Their value will overwrite any existing one in the additional set)
		qs.put(Parameters.collection.toString(), question.getCollection().getId());
		qs.put(Parameters.profile.toString(), question.getProfile());

		qs.put(Parameters.query.toString(), buildQuery());
		
		String gscope1 = buildGScope1();
		if (gscope1 != null && ! "".equals(gscope1)) {
			qs.put(Parameters.gscope1.toString(), buildGScope1());
		}
		
		// Remove from query string any parameter that will be passed as an environment variable
		for (String key : question.getEnvironmentVariables().keySet()) {
			qs.remove(key);
		}
		
		return toQueryString(qs);
		
	}
	
	@SneakyThrows(UnsupportedEncodingException.class)
	private static String toQueryString(Map<String, String> map) {
		StringBuffer out = new StringBuffer();
		for(Iterator<Entry<String, String>> it = map.entrySet().iterator(); it.hasNext();) {

			Entry<String, String> entry = it.next();
			if (entry.getKey().equals(RequestParameters.CLIVE) && entry.getValue() != null) {
				// FIXME clive is the only multi-valued parameter in PADRE
				// Maybe we should change that.
				String[] clives = entry.getValue().split(",");
				for (int i=0; i<clives.length; i++) {
					out.append(entry.getKey()+"="+URLEncoder.encode(clives[i], "UTF-8"));
					if (i+1 < clives.length) {
						out.append(DELIMITER);
					}
				}
			} else {
				if (entry.getValue() != null) {
					out.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
				} else {
					out.append(entry.getKey() + "=");
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
		StringBuffer query = new StringBuffer();
		if (question.getQuery() != null) {
			query.append(question.getQuery());
		}
		
		if (question.getQueryExpressions().size() > 0) {
			// Add additional query expressions
			query.append(" " + StringUtils.join(question.getQueryExpressions(), " "));
		}
		
		if (question.getMetaParameters().size() > 0) {
			// Add meta_* parameters transformed as query expressions
			for (String value : question.getMetaParameters()) {
				query.append(" " + value);
			}
		}
		
		if (withFacetConstraints && question.getFacetsQueryConstraints().size() > 0) {
			// Add query constraints for faceted navigation
			for (String value: question.getFacetsQueryConstraints()) {
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
		String facetGscopeConstraints = question.getFacetsGScopeConstraints();
		// Do we have gscope constraints due to faceted navigation ?
		if ( withFacetConstraints && facetGscopeConstraints!= null && ! "".equals(facetGscopeConstraints)) {
			
			// Do we have other gscope constraints (coming from query string)
			if (question.getAdditionalParameters().get(RequestParameters.GSCOPE1) != null) {
				// Combine them
				return question.getFacetsGScopeConstraints()
					// Only the [0] value is relevant
					+ (CharUtils.isAsciiNumeric(facetGscopeConstraints.charAt(facetGscopeConstraints.length()-1))
							? ","
							: "")
					+ question.getAdditionalParameters().get(RequestParameters.GSCOPE1)
					+ "+";
			} else {
				return question.getFacetsGScopeConstraints();
			}
		} else {
			if (question.getAdditionalParameters().get(RequestParameters.GSCOPE1) != null) {
				return question.getAdditionalParameters().get(RequestParameters.GSCOPE1);
			} else {
				return null;
			}
		}		
	}
	
	public static enum Parameters {
		collection,query,profile,gscope1;
	}
	
}
