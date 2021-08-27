package com.funnelback.plugin;

import com.funnelback.plugin.DeprecatedSearchLifeCyclePlugin;
import com.funnelback.plugin.search.SearchLifeCycleContext;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
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
public interface SearchLifeCyclePlugin extends DeprecatedSearchLifeCyclePlugin {

    /**
     * Runs just after the <code>hook_pre_process.groovy</code> hook and
     * before any input processing occurs. Manipulation of the query and
     * addition or modification of most question attributes can be made at this point.
     *
     * @param searchLifeCycleContext
     * @param transaction
     */
    default void preProcess(SearchLifeCycleContext searchLifeCycleContext, SearchTransaction transaction) {
        preProcess(transaction);
    }

    /**
     * Runs just after the <code>hook_pre_datafetch.groovy</code> hook and
     * after all of the input processing is complete, but just before the
     * query is submitted. This hook can be used to manipulate any additional
     * data model elements that are populated by the input processing. This
     * is most commonly used for modifying faceted navigation.
     *
     * @param searchLifeCycleContext
     * @param transaction
     */
    default void preDatafetch(SearchLifeCycleContext searchLifeCycleContext, SearchTransaction transaction) {
        preDatafetch(transaction);
    }

    /**
     * Runs just after the <code>hook_post_datafetch.groovy</code> hook which is just
     * after the response object is populated based on the raw XML return, but before
     * other response elements are built. This is most commonly used to
     * modify underlying data before the faceted navigation is built.
     *
     * @param searchLifeCycleContext
     * @param transaction
     */
    default void postDatafetch(SearchLifeCycleContext searchLifeCycleContext, SearchTransaction transaction) {
        postDatafetch(transaction);
    }

    /**
     * Runs just after the <code>hook_post_process.groovy</code> hook. This is used to
     * modify the final data model prior to rendering of the search results.
     *
     * @param searchLifeCycleContext
     * @param transaction
     */
    default void postProcess(SearchLifeCycleContext searchLifeCycleContext, SearchTransaction transaction) {
        postProcess(transaction);
    }

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
     * @param searchLifeCycleContext
     * @param transaction
     */
    default void preExtraSearchExecution(SearchLifeCycleContext searchLifeCycleContext, SearchTransaction transaction) {
        preExtraSearchExecution(transaction);
    }
}
