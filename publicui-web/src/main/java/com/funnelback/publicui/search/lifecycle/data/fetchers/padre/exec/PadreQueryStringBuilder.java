package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.apache.commons.lang.StringUtils;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;


/**
 * Helper to build a query string that will be passed to PADRE. 
 */
public class PadreQueryStringBuilder {

	private static final String DELIMITER = "&";
	
	public static String buildQueryString(SearchTransaction transaction) {
		Map<String, String[]> qs = new HashMap<String, String[]>();		
		qs.put(Parameters.collection.toString(), new String[] {transaction.getQuestion().getCollection().getId()});
		qs.put(Parameters.profile.toString(), new String[] {transaction.getQuestion().getProfile()});

		qs.put(Parameters.query.toString(), new String[] {buildQuery(transaction)});
		
		// Add any other parameter
		qs.putAll(transaction.getQuestion().getAdditionalParameters());
		
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
	 * Builds query expression from the various input parameters
	 * @param transaction
	 * @return
	 */
	public static String buildQuery(SearchTransaction transaction) {
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
		return query.toString();
	}
	
	@RequiredArgsConstructor
	public static class ParameterValues {
		public final String parameter;
		public final String[] values;
	}
	
	public static enum Parameters {
		collection,query,profile;
	}
	
}
