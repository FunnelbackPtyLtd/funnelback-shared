package com.funnelback.publicui.search.lifecycle.output;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Called after data fetching to process output / results.
 */
public interface OutputProcessor {

	public void process(final SearchTransaction searchTransaction) throws OutputProcessorException;
	
}
