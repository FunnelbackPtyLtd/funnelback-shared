package com.funnelback.publicui.search.model.curator.config;

/**
 * Interface for classes which configure the CuratorConfig tree.
 * 
 * Intended for use by a configurer which autowires in any dependencies the 
 * Triggers/Actions have.
 */
public interface Configurer {

    /** Perform configuration action on an object */
    void configure(Object objectToConfigure);

}
