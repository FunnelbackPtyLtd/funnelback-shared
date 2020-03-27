package com.funnelback.common.filter.jsoup;

import java.util.Map;

import org.jsoup.nodes.Document;

import com.google.common.collect.Multimap;

public interface JsoupFilterContext {

    /**
     * A representation of the setup details for this filter.
     * 
     * For example, where Funnelback is installed and what collection is being used,
     * as well as access to configuration settings.
     */
    JsoupFilterSetupContext getSetup();
    
    /**
     * The document being filtered.
     * 
     * Modifications may be made in place if needed, however it may be easier in many
     * cases to add metadata via the additionalMetadata Multimap below.
     */
    Document getDocument();
    
    /**
     * Metadata to be added to the document being filtered as a result of filtering. One metadata entry
     * with the same key as the map entry will be added for each entry in the value set.
     * 
     * This is a Multimap which supports multiple values for each key. See 
     * http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/Multimap.html
     * for details of the calls available, though you can probably just treat it like a normal map
     * if you don't need to replace existing values.
     * 
     * The current implementation will add the metadata by inserting HTML meta tags into the document
     * but in the future we may change this to store metadata separately to the content itself, so avoid
     * relying on a specific storage location.
     */
    Multimap<String, String> getAdditionalMetadata();
    
    /**
     * A map of custom data which may be used to communicate between filters in the chain.
     * Any filter may add, edit or remove entries for the document during filtering.
     * 
     * Note that the map and all content will be discarded once the document has been filtered.
     */
    Map<String, Object> getCustomData();  
    
}
