package com.funnelback.publicui.search.lifecycle;

/**
 * Top-level interface for processors of the Modern UI
 * 
 * @since 12.4
 */
public interface LifecycleProcessor {

    /**
     * @return The identifier of the processor, usually to be
     * displayed in performance metrics or debug logs.
     */
    public String getId();
    
}
