package com.funnelback.publicui.search.service;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;

import java.net.URI;
import java.util.Date;

/**
 * Interface to access index data
 */
public interface IndexRepository {
    
    /**
     * Keys used to retrieve some special values
     * from the <code>.bldinfo</code> map.
     */
    public enum BuildInfoKeys {
        /** PADRE version string (Starting with 'version') */
        version("version"),
        /** List of indexer arguments, separated by {@link #INDEXER_OPTIONS_SEPARATOR} */
        indexer_arguments("indexer_arguments"),
        /** Numbers of documents in the collection */
        Num_docs("Num_docs"),
        /** Average length of documents in the collection */
        Average_document_length("Average_document_length");
        
        BuildInfoKeys(String displayName) {
            this.displayName = displayName;
        }
        
        private String displayName = null;
        
        @Override
        public String toString() {
            if(displayName != null) return displayName;
            return name();
        }
    }
    
    /** Separator for the indexer options of the <code>.bldinfo</code> file */
    public static final String INDEXER_OPTIONS_SEPARATOR = "\n";
    
    /**
     * @param c
     * @return The last update date for the collection
     * @throws UnsupportedOperationException if the collection is a meta collection,
     * since they don't have a <code>index_time</code> file.
     */
    public Date getLastUpdated(String collectionId) throws UnsupportedOperationException;
    
    /**
     * Retrieve a value from the <code>.bldinfo<code> file.
     * @param key
     * @return
     * @throws UnsupportedOperationException if the collection is a meta collection,
     * since they don't have a <code>.bldinfo</code> file.
     */
    public String getBuildInfoValue(String collectionId, String key) throws UnsupportedOperationException;

    /**
     * Retrieve a single result and all its data from the index
     * @param collection Collection to get the result from
     * @param indexUri URI of the result to get
     * @return A result, or null if not found
     */
    public Result getResult(Collection collection, URI indexUri);
}
