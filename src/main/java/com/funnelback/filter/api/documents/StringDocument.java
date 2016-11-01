package com.funnelback.filter.api.documents;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.documents.DefaultBytesDocument;
import com.funnelback.filter.documents.DefaultStringDocument;
import com.funnelback.common.text.TextUtils;
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
     * should be used, otherwise a custom value can be built with {@link DocumentType#fromContentType(String)}.
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
    public static Optional<StringDocument> from(FilterableDocument filterableDocument) {
        if(filterableDocument instanceof StringDocument) {
            return Optional.of((StringDocument) filterableDocument);
        }
        
        try {
            byte[] content = (filterableDocument instanceof DefaultBytesDocument) ?
                ((DefaultBytesDocument) filterableDocument).getContent() : filterableDocument.getCopyOfContents();
            
            
             Charset charset = filterableDocument.getCharset()
                .orElseGet(() -> Charset.forName(TextUtils.getCharSet(new ByteArrayInputStream(content))));
            
             String stringContent = new String(content, charset);
            
             return Optional.of(new DefaultStringDocument(filterableDocument.getURI(), 
                                         filterableDocument.getMetadata(), 
                                         filterableDocument.getDocumentType(), 
                                         stringContent));
                
        } catch (Exception e) {
            logger.error("Could not convert bytes to string", e);
        }
        
        return Optional.empty();
        
    }
    
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
    public static StringDocument from(FilterableDocument filterableDocument, DocumentType documentType, String content) {       
        return new DefaultStringDocument(filterableDocument.getURI(), 
                                 filterableDocument.getMetadata(), 
                                 documentType, 
                                 content);
    }
    
}