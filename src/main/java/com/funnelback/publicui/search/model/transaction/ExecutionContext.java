package com.funnelback.publicui.search.model.transaction;

/**
 * <p>Context where the Modern UI is being executed</p>
 * 
 * <p>The Modern UI is usually deployed twice, once on the public port and once on the admin port,
 * this enum provides a way to distinguish these deployments</p>
 */
public enum ExecutionContext {
    /** Deployed on Admin port */
    Admin,
    /** Deployed on Public port */
    Public,
    /** Context may be unknown, usually when running under an IDE / Tomcat */
    Unknown;

}
