package com.funnelback.plugin;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * All methods will be called under the context of the the search UI
 *
 */
public interface SearchLifeCyclePlugin {
    
//    /**
//     * Called only when a search is being executed under the search UI.
//     * 
//     * The context contains:
//     * 
//     * "SEARCH_TRANSACTION"
//     * - type: com.funnelback.publicui.search.model.transaction.SearchTransaction
//     * - The search transaction which is can be edited for customisation.
//     * "HOOK_TYPE_S"
//     * - type: String
//     * - Defines when the hook is being called during the lifecycle can either be TODO.... 
//     * 
//     * Note called for cache copies. 
//     * 
//     * @param context
//     */
//    public default void searchHook(SearchTransaction searchTransaction) {
//        
//    }
    
    default void preProcess(SearchTransaction transaction) {}
    
    default void preDatafetch(SearchTransaction transaction) {}
    
    default void postDatafetch(SearchTransaction transaction) {}
    
    default void postProcess(SearchTransaction transaction) {}

    
    // TODO cache copies are harder because the pre hook does some work in checking if access is granted.
//    /**
//     * Called when a cached document is being served under the serach UI.
//     * 
//     * the context contains:
//     * "collection":
//     * - type: com.funnelback.publicui.search.model.collection.Collection
//     * - Configuration for the current collection.
//     * "document"
//     * - type: com.funnelback.common.io.store.Store.RecordAndMetadata<? extends Record<?>>
//     * 
//     * @param context
//     */
//    public default void cacheHook(Map<String, Object> context) {
//        
//    }
    
//    /**
//     * Allows the plugin to define query_processor_options that will be overridden by query processor
//     * set in collection.cfg, profile.cfg, padre_opts.cfg, in the URL, or internally e.g. for faceted navigation.
//     * 
//     * The context will contain:
//     * "SEARCH_TRANSACTION"
//     * - type: com.funnelback.publicui.search.model.transaction.SearchTransaction
//     * - The search transaction which is can be edited for customisation.
//// TODO this stuff needs to go to some other place free of the modern UI.
////     * "COLLECTON_CFG_OPTIONS":
////     * - type: Function<String, String>
////     * - This is a function which can be given key and will return the value for that key as set in collection.cfg
////     * ditto for PROFILE_CFG_OPTIONS
////     * Otherwise the context will be empty.
//     * 
//     * @param context
//     * @return the first query processor options given to padre that may be overridden by other options.
//     */
//    public default String defaultQueryProcessorOptions(Map<String, Object> context) {
//        return null;
//        
//    }
    

}
