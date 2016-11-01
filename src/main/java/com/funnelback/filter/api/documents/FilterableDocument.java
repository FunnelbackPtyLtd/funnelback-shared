package com.funnelback.filter.api.documents;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import com.funnelback.filter.api.DocumentType;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * A Document which can be filtered by the filter framwork.
 *
 * <p>Implementations must be immutable, this is to ensure that we can not have Filters which
 * edit a document yet claim to be skipped.</p>
 * 
 * <p>Further implementations should provide cloneWithX methods, to allow creating a new document
 * rather than expose a constructor which is likely to change. Clients of implementations of
 * this interface should avoid using constructors as they are likely to change.</p> 
 */
public interface FilterableDocument {

    /**
     * The URI of the document.
     * 
     * @return the URI of the document.
     */
    public URI getURI();
    
    /**
     * Metadata of the document.
     * 
     * <p>Metadata is accessible to the indexer with metamap configuration. Values may contain 
     * printable UTF-8 characters that do not contain new lines (line feeds) or carriage return.
     * Metadata keys must only consist of printable non whitespace ASCII characters.</p>
     * 
     * 
     * <p>The Content-Type metadata, if set, should be the content type of the original 
     * document. It must have no more than one value. Unlike getMimeType() this value will 
     * not change during filtering unless a filter is correcting the original content type. 
     * Metadata with keys that start with <pre>X-Fun</pre> should not be edited or removed or 
     * added.</p>
     * 
     * <p>For web crawls this will initially contain the HTTP headers returned from the server, thus 
     * keys which might conflict with existing standard HTTP headers should be avoided.</p>  
     * 
     * @return a immutable map of the documents metadata.
     */
    public ImmutableListMultimap<String, String> getMetadata();
    
    /**
     * Gets a mutable copy of the documents metadata.
     *  
     * @return a mutable copy of the documents metadata.
     */
    public ListMultimap<String, String> getCopyOfMetadata();
    
    
    /**
     * Returns a copy of the documents content as bytes.
     * 
     * @return a mutable copy of the documents contents.
     */
    byte[] getCopyOfContents();
    
    /**
     * Returns the possible charset of the document.
     *  
     * <p>If a filter edits the charset of the bytes, the filter must also
     * ensure the returned document produces the new charset from this method</p> 
     * 
     * @return the charset of the current content of the document or empty if it is unknown.
     */
    public Optional<Charset> getCharset();
    
    /**
     * Returns the document type of this filterable document, this may not be the original document type.
     * 
     * <p>If a filter changes the type of a document e.g. CSV to JSON the document type
     * of the Filterable Document should be changed rather than the Content-Type in the headers.</p>
     * 
     * <p>If the implementation describes a charset it should be ignored with preference to
     * the charset returned by {@link #getCharset()}.</p>
     * @return the document type for current document content (not the original document type
     * which may have changed with filtering)
     */
    public DocumentType getDocumentType();
    
    
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
