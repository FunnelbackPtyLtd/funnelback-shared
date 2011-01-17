package com.funnelback.publicui.search.lifecycle.input.processors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

@Component("dummyInputProcessor")
@lombok.extern.apachecommons.Log
public class Dummy implements InputProcessor {

	@Override
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) {
		log.info("Processing input...");
	}

}
