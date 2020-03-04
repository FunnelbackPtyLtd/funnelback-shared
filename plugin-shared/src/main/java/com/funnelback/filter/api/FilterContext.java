package com.funnelback.filter.api;

import java.util.Optional;
import java.util.Set;

/**
 * An immutable context supplied to the filters.
 * 
 * <p>The context can be used to get values from the collection configuration the filter 
 * is running under.</p>
 *
 */
public interface FilterContext {
    
    /**
     * Gets the name of the collection under which the filter is run.
     * 
     * @return the collection name the filter is running under.
     */
    public String getCollectionName();
    
    /**
     * Gets the set of all configuration keys for collection the filter is running under.
     * 
     * @return all configurations keys.
     */
    public Set<String> getConfigKeys();
    
    /**
     * Gets the value of the configuration setting for the collection the filter is running under.
     * 
     * @param key used to lookup the configuration value.
     * @return the value for the key from the configuration if the key exists in the configuration
     * otherwise returns empty.
     */
    public Optional<String> getConfigValue(String key);
    
    /**
     * Provides a DocumentTypeConverter, used to convert between filterable document types.
     * 
     * The implementation provided under testing may differ from the implementation provided
     * under Funnelback e.g. during a crawl.
     * 
     * @return
     */
    public FilterDocumentConverter getDocumentTypeConverter();
    
    /**
     * Provides a DocumentTypeFactory, may be used to construct  
     * @return
     */
    public DocumentTypeFactory getDocumentTypeFactory();
}
