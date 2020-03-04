package com.funnelback.filter.api.documents;

import java.util.Optional;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.DocumentTypeFactory;
import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterDocumentConverter;
import com.google.common.collect.ListMultimap;

/**
 * A raw byte document. 
 *
 */
public interface BytesDocument extends FilterableDocument {
    
    /**
     * Creates a clone of the document with the given content.
     * 
     * <p>If the charset is known, it usually better to create a new StringDocument using
     * {@link FilterDocumentConverter#toStringDocument(FilterableDocument)} as most filters work on 
     * Strings.</p>
     * 
     * @param documentType the type of content. Typically a predefined type from {@link DocumentType}
     * should be used, otherwise a custom value can be built with {@link DocumentTypeFactory#fromContentTypeHeader(String)}
     * available from the {@link FilterContext}.
     * @param charset The charset of the content for example {@code Optional.of(StandardCharsets.UTF_8)}
     * if the charset is unknown set {@code Optional.empty()}.
     * @param content The content to set the document to.
     * @return a clone of this document with the given content.
     */
    public BytesDocument cloneWithContent(DocumentType documentType, Optional<Charset> charset, byte[] content);
    
    /**
     * Gets the contents of the document as an input stream.
     * 
     * <p>This is probably cheaper than getting a copy of the bytes and wrapping
     * that in your own input stream.</p> 
     * 
     * @return the contents of the document as an input stream.
     */
    public InputStream contentAsInputStream();
    
    /**
     * {@inheritDoc}
     */
    public BytesDocument cloneWithURI(URI uri);
    
    /**
     * {@inheritDoc}
     */
    public BytesDocument cloneWithMetadata(ListMultimap<String, String> metadata);
    
}
