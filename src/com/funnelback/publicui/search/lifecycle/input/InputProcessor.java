package com.funnelback.publicui.search.lifecycle.input;

import javax.servlet.http.HttpServletRequest;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Called prior to fetching data, to prepare the data fetching
 */
public interface InputProcessor {

	public void process(final SearchTransaction searchTransaction, final HttpServletRequest request);
	
}
