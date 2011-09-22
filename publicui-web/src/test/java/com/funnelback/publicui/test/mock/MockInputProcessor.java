package com.funnelback.publicui.test.mock;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MockInputProcessor implements InputProcessor {

	@Getter @Setter
	private boolean traversed = false;
	
	@Setter
	private boolean throwError = false;
	
	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		traversed = true;
		if (throwError) {
			throw new InputProcessorException(MockInputProcessor.class.getName(), null);
		}
	}
	
	

}
