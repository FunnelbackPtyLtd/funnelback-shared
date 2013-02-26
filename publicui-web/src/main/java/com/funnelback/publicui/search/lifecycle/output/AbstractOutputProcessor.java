package com.funnelback.publicui.search.lifecycle.output;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Base class for {@link OutputProcessor}
 * 
 * @since 12.4
 */
public abstract class AbstractOutputProcessor implements OutputProcessor {

	@Override
	public String getId() {
		return this.getClass().getSimpleName();
	}

	@Override
	public abstract void processOutput(SearchTransaction searchTransaction)
			throws OutputProcessorException;

}
