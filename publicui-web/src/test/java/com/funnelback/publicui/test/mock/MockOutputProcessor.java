package com.funnelback.publicui.test.mock;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MockOutputProcessor implements OutputProcessor {

	@Getter @Setter
	private boolean traversed = false;
	
	@Setter
	private boolean throwError = false;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		traversed = true;
		if (throwError) {
			throw new OutputProcessorException(MockOutputProcessor.class.getName(), null);
		}
	}

}
