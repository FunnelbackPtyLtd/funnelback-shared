package com.funnelback.plugin.index.consumers;

import com.funnelback.plugin.index.model.metadatamapping.MetadataSourceType;
import com.funnelback.plugin.index.model.metadatamapping.MetadataType;

@FunctionalInterface
public interface MetadataMappingConsumer {

    /**
     * 
     * @param metadataClass the name of the metadata class which may be used at query time. Must be less than
     * 65 characters and must be ASCII alpha numeric.
     * @param type the type of the metadata class, e.g. NUMERICAL for numeric values
     * @param sourceType where the metadata is coming from e.g. HTML_OR_HTTP_HEADERS if the metadata is coming
     * from <code>&#x3C;meta name=&#x22;author&#x22; content=&#x22;John Doe&#x22;&#x3E;</code>
     * @param locator the location to get the metadata from e.g. when getting metadata from HTML: 
     * <code>&#x3C;meta name="author" content="John Doe"&#x3E;</code> the locator would be
     * "author".
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    public void map(String metadataClass, MetadataType type, MetadataSourceType sourceType, String locator)
        throws IllegalArgumentException;
    
}
