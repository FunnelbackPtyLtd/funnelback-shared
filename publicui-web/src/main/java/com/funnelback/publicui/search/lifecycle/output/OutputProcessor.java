package com.funnelback.publicui.search.lifecycle.output;

import com.funnelback.publicui.search.lifecycle.LifecycleProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Called after data fetching to process response / results.
 */
public interface OutputProcessor extends LifecycleProcessor {

	public void processOutput(final SearchTransaction searchTransaction) throws OutputProcessorException;
	
}
