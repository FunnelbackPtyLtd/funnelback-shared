package com.funnelback.publicui.log.service;

import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;

/**
 * Logs clicks, queries, etc.
 */
public interface LogService {

	public void logClick(ClickLog log);
	
	public void logContextualNavigation(ContextualNavigationLog log);
	
}
