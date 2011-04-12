package com.funnelback.publicui.search.lifecycle;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Processes a {@link SearchTransaction}. Runs the different
 * processors and data fetchers.
 */
public interface SearchTransactionProcessor {

	/**
	 * Processes a {@link SearchQuestion} into a {@link SearchTransaction}
	 * @param q
	 * @return
	 */
	public SearchTransaction process(SearchQuestion q);
	
}
