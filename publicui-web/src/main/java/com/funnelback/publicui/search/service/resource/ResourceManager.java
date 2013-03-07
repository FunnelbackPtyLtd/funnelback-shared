package com.funnelback.publicui.search.service.resource;

import java.io.IOException;

/**
 * Manages resources: Abstract how they're loaded
 * and possibly cached.
 */
public interface ResourceManager {

    /**
     * Deserialise a resource and returns the corresponding Java object
     * @param resource Resource to deserialise from
     * @return The deserialised Java object
     * @throws IOException
     */
    public <T> T load(ParseableResource<T> resource) throws IOException;
    
    /**
     * Deserialise a resource and returns the corresponding Java objet
     * @param resource Resource to deserialise from
     * @param valueIfNonExistent Default value to return if the resource doesn't exist
     * @return The deserialised object, or valueIfNonExistent
     * @throws IOException
     */
    public <T> T load(ParseableResource<T> resource, T valueIfNonExistent) throws IOException;
    
}
