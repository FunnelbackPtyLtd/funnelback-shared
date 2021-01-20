package com.funnelback.filter.api;

import java.util.Optional;

import com.funnelback.filter.api.documents.BytesDocument;
import com.funnelback.filter.api.documents.FilterableDocument;
import com.funnelback.filter.api.documents.StringDocument;

public interface FilterDocumentFactory {

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
    
    /**
     * Constructs a StringDocument from a FilterableDocument, with the given mimeType and content.
     * 
     * <p>This may be used when converting from binary to a string form, such as pdf to html.</p>
     * 
     * @param filterableDocument which the resulting document will be cloned from.
     * @param documentType of the given content.
     * @param content the new content of the document as a String.
     * @return A StringDocument which is the same as the given filterableDocument except with
     * the given documentType and content.
     */
    public StringDocument toStringDocument(FilterableDocument filterableDocument, DocumentType documentType, String content);
    
}
