package com.funnelback.publicui.search.service.resource;

import java.io.IOException;

/**
 * <p>A resource that can be parsed into a Java Object.</p>
 *
 */
public interface ParseableResource<T> {

    /** Parses the resource and returns its Java representation */
    public T parse() throws IOException;
    
    /**
     * Whether the resource is more recent than the provided
     * timestamp and should be parsed again
     * @param timestamp
     * @return
     */
    public boolean isStale(long timestamp);
    
    /**
     * Unique key representing this resource, used
     * to cache it.
     * @return
     */
    public Object getCacheKey();
    
    /**
     * Whether the underlying resource exists or not.
     * @return
     */
    public boolean exists();
    
}
