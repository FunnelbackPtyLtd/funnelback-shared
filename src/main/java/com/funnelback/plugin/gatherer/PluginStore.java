package com.funnelback.plugin.gatherer;

import java.net.URI;

import com.google.common.collect.ListMultimap;

public interface PluginStore {

    
    /**
     * Store content under a URI.
     * 
     * @param uri The URI for the content, typically a URL.
     * @param content The content itself.
     * @param metadata Extra data about the document which may be mapped to
     * metadata classes. This map must contain exactly one Content-Type metadata 
     * value, and the value must follow the HTTP Content-Type header syntax. Further
     * the Content-Type value must declare the type of the content and if the Charset
     * of the content.
     */
    public void store(URI uri,
        byte[] content,
        ListMultimap<String, String> metadata);
    
}
