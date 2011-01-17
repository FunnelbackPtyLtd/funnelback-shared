package com.funnelback.publicui.log.service;

import com.funnelback.publicui.search.model.log.ClickLog;

/**
 * Logs clicks, queries, etc.
 */
public interface LogService {

	public void logClick(ClickLog log);
	
}
