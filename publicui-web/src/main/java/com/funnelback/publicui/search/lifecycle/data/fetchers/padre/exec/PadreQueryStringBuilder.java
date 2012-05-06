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
import com.funnelback.publicui.utils.MapUtils;


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
		Map<String, String[]> qs = new TreeMap<String, String[]>();
		
		// Add any additional parameter
		qs.putAll(question.getAdditionalParameters());
		
		// Then craft our owns (Their value will overwrite any existing one in the additional set)
		qs.put(Parameters.collection.toString(), new String[] {question.getCollection().getId()});
		qs.put(Parameters.profile.toString(), new String[] {question.getProfile()});

		qs.put(Parameters.query.toString(), new String[] {buildQuery()});
		
		String gscope1 = buildGScope1();
		if (gscope1 != null && ! "".equals(gscope1)) {
			qs.put(Parameters.gscope1.toString(), new String[] {buildGScope1()});
		}
		
		// Remove from query string any parameter that will be passed as an environment variable
		for (String key : question.getEnvironmentVariables().keySet()) {
			qs.remove(key);
		}
		
		return toQueryString(qs);
		
	}
	
	@SneakyThrows(UnsupportedEncodingException.class)
	private static String toQueryString(Map<String, String[]> map) {
		StringBuffer out = new StringBuffer();
		for(Iterator<Entry<String, String[]>> it = map.entrySet().iterator(); it.hasNext();) {

			Entry<String, String[]> entry = it.next();
			if (entry.getValue() != null) {
				String[] values = entry.getValue();
				for (int i=0; i<values.length; i++) {
					out.append(entry.getKey()).append("=").append(URLEncoder.encode(values[i], "UTF-8"));
					if (i+1<values.length) {
						out.append("&");
					}
				}
			} else {
				out.append(entry.getKey()).append("=");
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
			query.append(" ").append(StringUtils.join(question.getQueryExpressions(), " "));
		}
		
		if (question.getMetaParameters().size() > 0) {
			// Add meta_* parameters transformed as query expressions
			for (String value : question.getMetaParameters()) {
				query.append(" ").append(value);
			}
		}
		
		if (withFacetConstraints && question.getFacetsQueryConstraints().size() > 0) {
			// Add query constraints for faceted navigation
			for (String value: question.getFacetsQueryConstraints()) {
				query.append(" ").append(value);
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
					+ MapUtils.getFirstString(question.getAdditionalParameters(), RequestParameters.GSCOPE1, null)
					+ "+";
			} else {
				return question.getFacetsGScopeConstraints();
			}
		} else {
			if (question.getAdditionalParameters().get(RequestParameters.GSCOPE1) != null) {
				return MapUtils.getFirstString(question.getAdditionalParameters(), RequestParameters.GSCOPE1, null);
			} else {
				return null;
			}
		}		
	}
	
	public static enum Parameters {
		collection,query,profile,gscope1;
	}
	
}
