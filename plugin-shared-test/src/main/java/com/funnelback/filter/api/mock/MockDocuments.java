package com.funnelback.filter.api.mock;

import java.util.Optional;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.documents.BytesDocument;
import com.funnelback.filter.api.documents.StringDocument;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

/**
 * Use the static methods in this class to get mock document for testing.
 *
 */
public interface MockDocuments {

    /**
     * Constructs a mock StringDocument
     * 
     * <p>cloneWith methods may be used on the returned document to build up
     * the required document for testing.</p>
     * 
     * @param uri The uri of the document.
     * @param documentType the mimeType of the document
     * @param content the content of the document.
     * @return a mocked StringDocument
     */
    public static StringDocument mockStringDoc(String uri, DocumentType documentType, String content) {
        try {
            return new MockStringDocument(new URI(uri), HashMultimap.create(), documentType, content);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Creates an empty byte document for test.
     * <p>Subsequent calls to this will return a document with the same URI, mime type, 
     * encoding and content. The following is true
     * <code>
     * mockEmptyStringDoc().equals(mockEmptyStringDoc());
     * </code>
     * 
     * <p>However these values may change between versions.</p>
     * 
     * @return a mock document with an empty string as the content.
     */
    public static StringDocument mockEmptyStringDoc() {
        return mockStringDoc("https://good.devs/write/tests", 
                DocumentType.MIME_UNKNOWN, 
                "");
    }
    
    /**
     * Creates a mock ByteDocument
     * 
     *  <p>cloneWith methods may be used on the returned document to build up
     * the required document for testing.</p>
     * 
     * @param uri to set the on the returned document.
     * @param documentType of the content.
     * @param charset of the content if known otherwise {@link Optional#empty()}
     * @param content to be set on the returned document.
     * @return a BytesDocument with the given uri, documentType, charset and content.
     */
    public static BytesDocument mockByteDoc(String uri, DocumentType documentType, Optional<Charset> charset, byte[] content) {
        try {
            return new MockBytesDocument(new URI(uri), ArrayListMultimap.create(), documentType, charset, content);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * Creates an empty byte document for test.
     * <p>Subsequent calls to this will return a document with the same URI, mime type 
     * and content. The following is true
     * <code>
     * mockEmptyByteDoc().equals(mockEmptyByteDoc());
     * </code>
     * 
     * <p>Values in the document may change between versions.</p>
     * 
     * @return a ByteDocument where the content is a zero length byte array.
     */
    public static BytesDocument mockEmptyByteDoc() {
        return mockByteDoc("https://good.devs/write/tests", 
                DocumentType.MIME_UNKNOWN, 
                Optional.empty(), 
                new byte[0]);
    }
}
