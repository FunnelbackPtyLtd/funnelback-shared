package com.funnelback.plugin;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 *
 * @deprecated replaced by {@link SearchLifeCyclePlugin}
 *
 * This interface is only used for backwards compatability purpose.
 * Dev should only implement methods from {@link SearchLifeCyclePlugin}.
 * And must not implement both the old and new methods with the same name.
 *
 */
public interface DeprecatedSearchLifeCyclePlugin {
    
    /**
     * Runs just after the <code>hook_pre_process.groovy</code> hook and 
     * before any input processing occurs. Manipulation of the query and 
     * addition or modification of most question attributes can be made at this point.
     * 
     * @param transaction
     */
    @Deprecated
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
    @Deprecated
    public default void preDatafetch(SearchTransaction transaction) {}
    
    /**
     * Runs just after the <code>hook_post_datafetch.groovy</code> hook which is just 
     * after the response object is populated based on the raw XML return, but before 
     * other response elements are built. This is most commonly used to 
     * modify underlying data before the faceted navigation is built.
     * 
     * @param transaction
     */
    @Deprecated
    public default void postDatafetch(SearchTransaction transaction) {}
    
    /**
     * Runs just after the <code>hook_post_process.groovy</code> hook. This is used to 
     * modify the final data model prior to rendering of the search results.
     * 
     * @param transaction
     */
    @Deprecated
    public default void postProcess(SearchTransaction transaction) {}
    
    /**
     * Runs just after the search life cycle section <code>extraSearchesInputProcessor</code>
     * which adds configured extra searches from <code>ui.modern.extra_searches</code> to
     * the main search transaction.
     * 
     * This will run on all search transactions just before extra searches are executed.
     * 
     * This hook can be used for adding or removing extra searches that would be executed under
     * the given transaction. This hook does not need to be used for editing the {@link SearchQuestion}
     * of the extra searches since that can be done with the other hooks.
     * 
     * @param transaction
     */
    @Deprecated
    public default void preExtraSearchExecution(SearchTransaction transaction) {}
}
