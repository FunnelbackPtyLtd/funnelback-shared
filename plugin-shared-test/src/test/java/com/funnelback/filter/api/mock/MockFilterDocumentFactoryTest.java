package com.funnelback.filter.api.mock;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.documents.BytesDocument;
import com.funnelback.filter.api.documents.FilterableDocument;
import com.funnelback.filter.api.documents.StringDocument;

public class MockFilterDocumentFactoryTest {

    MockFilterDocumentFactory underTest = new MockFilterDocumentFactory();
    
    @Test
    public void testToBytesDocument() {
        FilterableDocument inDoc = MockDocuments.mockEmptyStringDoc().cloneWithStringContent(DocumentType.MIME_TEXT_PLAIN, "hello");
        FilterableDocument outDoc = underTest.toBytesDocument(inDoc);
        Assertions.assertEquals("hello", new String(outDoc.getCopyOfContents(), UTF_8), "Check the bytes turn back into the original string");
    }
    
    @Test
    public void toStringDocument_knownCharset() {
        BytesDocument inDoc = MockDocuments.mockEmptyByteDoc()
                .cloneWithContent(DocumentType.MIME_HTML_TEXT, Optional.of(UTF_8), "hello".getBytes());
        
        Assertions.assertTrue(underTest.toStringDocument(inDoc).isPresent(), "In the case that the charset is known we should get back a document");
    }
    
    @Test
    public void toStringDocument_unknownCharset() {
        BytesDocument inDoc = MockDocuments.mockEmptyByteDoc()
                .cloneWithContent(DocumentType.MIME_HTML_TEXT, Optional.empty(), "hello".getBytes());
        
        Assertions.assertTrue(underTest.toStringDocument(inDoc).isEmpty(), "When the charset is unknown the mock wont guess the charset and will return optional empty.");
    }
    
    @Test
    public void toStringDocument_settingContent() {
        BytesDocument inDoc = MockDocuments.mockEmptyByteDoc()
                .cloneWithContent(DocumentType.MIME_UNKNOWN, Optional.empty(), "old".getBytes());
        
        StringDocument outDoc = underTest.toStringDocument(inDoc, DocumentType.MIME_HTML_TEXT, "new");
        
        Assertions.assertEquals("new", outDoc.getContentAsString(), "Check the content was replaced.");
        Assertions.assertEquals(DocumentType.MIME_HTML_TEXT, outDoc.getDocumentType(), "Check the document type was updated.");
        Assertions.assertTrue(outDoc.getCharset().isPresent(), "All string documents have a charset.");
    }
}