package com.funnelback.publicui.search.lifecycle.input.processors;

import javax.servlet.http.HttpServletRequest

import org.springframework.stereotype.Component

import com.funnelback.publicui.search.lifecycle.input.InputProcessor
import com.funnelback.publicui.search.model.transaction.SearchTransaction

@Component("groovySampleInputProcessor")
class GroovySample implements InputProcessor {

	void process(final SearchTransaction searchTransaction, final HttpServletRequest request) {
		println ("GroovySample: Processing Input...");
	}
	
}