package com.funnelback.publicui.search.service.log;

import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;

/**
 * Logs clicks, queries, etc.
 */
public interface LogService {

    public void logClick(ClickLog cl);
    
    public void logContextualNavigation(ContextualNavigationLog cnl);
    
    public void logPublicUIWarning(PublicUIWarningLog warning);
    
}
