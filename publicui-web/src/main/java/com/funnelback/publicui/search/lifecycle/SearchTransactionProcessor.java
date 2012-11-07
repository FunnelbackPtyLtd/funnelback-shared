package com.funnelback.publicui.search.lifecycle;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>Processes a {@link SearchTransaction}. Runs the different
 * processors and data fetchers.<p>
 * 
 * <p>See the Spring XML configuration file to find out where the default
 * implementation is.</p>
 */
public interface SearchTransactionProcessor {

	/**
	 * Processes a {@link SearchQuestion} into a {@link SearchTransaction}
	 * @param q
	 * @return
	 */
	public SearchTransaction process(SearchQuestion q);
	
}
