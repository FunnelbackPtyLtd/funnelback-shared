package com.funnelback.filter.api.documents;

import java.net.URI;

import com.google.common.collect.ListMultimap;


/**
 * A Document which can be filtered by the filter framework.
 *
 * <p>All documents which can be filtered must extend this interface.</p>
 *  
 * <p>Implementations must be immutable, this is to ensure that we can not have Filters which
 * edit a document yet claim to be skipped.</p>
 * 
 * <p>Further implementations should provide cloneWithX methods, to allow creating a new document
 * rather than expose a constructor which is likely to change. Clients of implementations of
 * this interface should avoid using constructors as they are likely to change.</p> 
 */
public interface FilterableDocument extends NoContentDocument {
    
    /**
     * Returns a copy of the documents content as bytes.
     * 
     * @return a mutable copy of the documents contents.
     */
    byte[] getCopyOfContents();
    
    /**
     * Gets a clone of the document with a different URI.
     * @param uri the document returned document will have.
     * @return A new document with the given URI.
     */
    public FilterableDocument cloneWithURI(URI uri);
    
    /**
     * Gets a clone of the document with the given metadata.
     * 
     * <p>Note that the returned document will only have the metadata given as a paramater to
     * this method. Typically the result of {@link #getCopyOfMetadata()} should be manipulated 
     * then passed to this function to preserve existing metadata.</p>
     * 
     * @param metadata The only metadata that the returbed document will have.
     * @return A new document with the given headers.
     */
    public FilterableDocument cloneWithMetadata(ListMultimap<String, String> metadata);
    
}
