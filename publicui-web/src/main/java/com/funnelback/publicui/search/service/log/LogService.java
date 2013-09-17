package com.funnelback.publicui.search.service.log;

import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.FacetedNavigationLog;
import com.funnelback.publicui.search.model.log.InteractionLog;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;

/**
 * Logs clicks, queries, etc.
 */
public interface LogService {

    /**
     * Log a click
     * @param cl {@link Click} to log
     */
    public void logClick(ClickLog cl);
    
    /**
     * Log a contextual navigation click
     * @param cnl The {@link ContextualNavigationLog} to log
     */
    public void logContextualNavigation(ContextualNavigationLog cnl);
    
    /**
     * Log a faceted navigation click
     * @param fnl The {@link FacetedNavigationLog} to log
     */
    public void logFacetedNavigation(FacetedNavigationLog fnl);
    
    /**
     * Log a warning in the Public UI warning file
     * @param warning Warning to log
     */
    public void logPublicUIWarning(PublicUIWarningLog warning);
    
    /**
     * Log a user interaction (query completion, etc.)
     * @param interactionLog {@link InteractionLog} to log
     */
    public void logInteraction(InteractionLog interactionLog);
    
}
