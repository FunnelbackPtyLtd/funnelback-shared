package com.funnelback.publicui.search.lifecycle.input;

import com.funnelback.publicui.search.lifecycle.LifecycleProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Called prior to fetching data, to prepare the data fetching
 */
public interface InputProcessor extends LifecycleProcessor {

	public void processInput(final SearchTransaction searchTransaction)
			throws InputProcessorException;

}
