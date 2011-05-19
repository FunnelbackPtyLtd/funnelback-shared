package com.funnelback.publicui.search.lifecycle.output.processors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Waits for any extra search to complete
 */
@Component("extraSearchesJoinOutputProcessor")
public class ExtraSearchesJoin implements OutputProcessor {

	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction != null) {
			searchTransaction.joinExtraSearches();
		}
	}

}
