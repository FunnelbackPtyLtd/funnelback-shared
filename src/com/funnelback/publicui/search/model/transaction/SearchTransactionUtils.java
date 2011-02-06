package com.funnelback.publicui.search.model.transaction;

import com.funnelback.common.padre.ResultPacket;
import com.funnelback.publicui.search.model.Collection;

public class SearchTransactionUtils {

	/**
	 * @param st
	 * @return true if the {@link SearchTransaction} is non null and has a
	 *  {@link SearchQuestion}
	 */
	public static boolean hasQuestion(SearchTransaction st) {
		return st != null && st.getQuestion() != null;
	}
	
	/**
	 * @param st
	 * @return true if the {@link SearchTransaction} is non null and has a {@link SearchQuestion} and
	 * a non-null query.
	 */
	public static boolean hasQuery(SearchTransaction st) {
		return(st != null && st.getQuestion() != null && st.getQuestion().getQuery() != null);
	}
	
	/**
	 * 
	 * @param st
	 * @return true if the {@link SearchTransaction} is non null and has a {@link SearchQuestion} and
	 * a non-null {@link Collection}.
	 */
	public static boolean hasCollection(SearchTransaction st) {
		return(st != null && st.getQuestion() != null && st.getQuestion().getCollection() != null); 
	}
	
	/**
	 * 
	 * @param st
	 * @return true if the {@link SearchTransaction} is non null and has a {@link SearchQuestion}, 
	 * a non-null query and a non-null {@link Collection}.
	 */
	public static boolean hasQueryAndCollection(SearchTransaction st) {
		return hasQuery(st) && hasCollection(st);
	}
	
	/**
	 *
	 * @param st
	 * @return true if the {@link SearchTransaction} is non null and has a {@link SearchResponse}.
	 */
	public static boolean hasResponse(SearchTransaction st) {
		return st != null && st.hasResponse();
	}
	
	/**
	 * 
	 * @param st
	 * @return true if the {@link SearchTransaction} is non null and has a {@link SearchResponse} which
	 * contains a {@link ResultPacket} and has more than zero results.
	 */
	public static boolean hasResults(SearchTransaction st) {
		return hasResponse(st) && st.getResponse().hasResultPacket() && st.getResponse().getResultPacket().hasResults();
	}
	
}
