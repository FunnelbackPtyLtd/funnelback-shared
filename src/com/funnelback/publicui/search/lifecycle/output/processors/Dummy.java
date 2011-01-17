package com.funnelback.publicui.search.lifecycle.output.processors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

@Component("dummyOutputProcessor")
@lombok.extern.apachecommons.Log
public class Dummy implements OutputProcessor {
	
	@Override
	public void process(SearchTransaction searchTransaction) {
		log.info("Processing output...");
	}

}
