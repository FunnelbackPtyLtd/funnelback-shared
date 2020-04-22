package com.funnelback.plugin;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Plugins can implement this to add logic to a search life cycle.
 * 
 * A plugin must set in the plugins properties file a line that is:
 * com.funnelback.plugin.SearchLifeCyclePlugin=name.of.class.from.Plugin.
 * 
 * All methods will be called under the context of the the search UI
 *
 */
public interface SearchLifeCyclePlugin {
    
    /**
     * Runs just after the <code>hook_pre_process.groovy</code> hook and 
     * before any input processing occurs. Manipulation of the query and 
     * addition or modification of most question attributes can be made at this point.
     * 
     * @param transaction
     */
    public default void preProcess(SearchTransaction transaction) {}
    
    /**
     * Runs just after the <code>hook_pre_datafetch.groovy</code> hook and
     * after all of the input processing is complete, but just before the 
     * query is submitted. This hook can be used to manipulate any additional 
     * data model elements that are populated by the input processing. This 
     * is most commonly used for modifying faceted navigation.
     * 
     * @param transaction
     */
    public default void preDatafetch(SearchTransaction transaction) {}
    
    /**
     * Runs just after the <code>hook_post_datafetch.groovy</code> hook which is just 
     * after the response object is populated based on the raw XML return, but before 
     * other response elements are built. This is most commonly used to 
     * modify underlying data before the faceted navigation is built.
     * 
     * @param transaction
     */
    public default void postDatafetch(SearchTransaction transaction) {}
    
    /**
     * Runs just after the <code>hook_post_process.groovy</code> hook. This is used to 
     * modify the final data model prior to rendering of the search results.
     * 
     * @param transaction
     */
    public default void postProcess(SearchTransaction transaction) {}
}
