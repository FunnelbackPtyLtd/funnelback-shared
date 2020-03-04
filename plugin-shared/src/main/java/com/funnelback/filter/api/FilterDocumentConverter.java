package com.funnelback.filter.api;

import java.util.Optional;

import com.funnelback.filter.api.documents.BytesDocument;
import com.funnelback.filter.api.documents.FilterableDocument;
import com.funnelback.filter.api.documents.StringDocument;

public interface FilterDocumentConverter {

    /**
     * Converts a FilterableDocument into a RawFilterableDocument
     * 
     * @param filterableDocument used to construct the ByteDocument.
     * @return a ByteDocument constructed from the given document.
     */
    public BytesDocument toBytesDocument(FilterableDocument filterableDocument);
    
    /**
     * Attempts to create a StingDocument from the given document.
     * 
     * <p>Makes a clone of the given document and converts the document content to a
     * String. This will use the charset of the document if set otherwise it will guess
     * the charset. If the charset could not be guessed or an error occurs while converting
     * the content to a String then an optional empty is returned.</p>
     * 
     * @param filterableDocument the document to make a StringDocument from.
     * @return A StringDocument copy of the given document or empty if a string document
     * could not be created.
     */
    public Optional<StringDocument> toStringDocument(FilterableDocument filterableDocument);
    
    
}
