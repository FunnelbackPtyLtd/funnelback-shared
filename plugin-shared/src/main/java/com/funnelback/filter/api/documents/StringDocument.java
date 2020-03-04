package com.funnelback.filter.api.documents;

import java.util.Optional;

import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.DocumentTypeFactory;
import com.funnelback.filter.api.FilterContext;
import com.google.common.collect.ListMultimap;

/**
 * A filterable String Document
 * 
 */
public interface StringDocument extends FilterableDocument {
    
    static final Logger logger = LogManager.getLogger(StringDocument.class);
    
    /**
     * Gets the document content as a string.
     * 
     * @return the content of the document as a string.
     */
    public String getContentAsString();

    @Override
    public StringDocument cloneWithURI(java.net.URI uri);
    
    @Override
    public StringDocument cloneWithMetadata(ListMultimap<String, String> metadata);
    
    /**
     * Creates a clone of the document with the given content and document type.
     * @param documentType the type of content. Typically a predefined type from {@link DocumentType}
     * should be used, otherwise a custom value can be built with {@link DocumentTypeFactory#fromContentTypeHeader(String)}
     * available from the {@link FilterContext}.
     * @param content The new content.
     * @return a copy of the document with the new content and DocumentType.
     */
    public StringDocument cloneWithStringContent(DocumentType documentType, String content);

    /**
     * Charset of a StringDocument are always UTF-8, if the document contains any indication of charset it must be UTF-8
     * 
     * @return the charset of a document which is always UTF-8.
     */
    public Optional<Charset> getCharset();
}