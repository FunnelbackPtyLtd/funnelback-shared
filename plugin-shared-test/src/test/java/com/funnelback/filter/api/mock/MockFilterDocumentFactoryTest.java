package com.funnelback.filter.api.mock;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("Check the bytes turn back into the original string",
            "hello", new String(outDoc.getCopyOfContents(), UTF_8));
    }
    
    @Test
    public void toStringDocument_knownCharset() {
        BytesDocument inDoc = MockDocuments.mockEmptyByteDoc()
                .cloneWithContent(DocumentType.MIME_HTML_TEXT, Optional.of(UTF_8), "hello".getBytes());
        
        Assert.assertTrue("In the case that the charset is known we should get back a document",
            underTest.toStringDocument(inDoc).isPresent());
    }
    
    @Test
    public void toStringDocument_unknownCharset() {
        BytesDocument inDoc = MockDocuments.mockEmptyByteDoc()
                .cloneWithContent(DocumentType.MIME_HTML_TEXT, Optional.empty(), "hello".getBytes());
        
        Assert.assertTrue("When the charset is unknown the mock wont guess the charset and will return optionl empty.",
            underTest.toStringDocument(inDoc).isEmpty());
    }
    
    @Test
    public void toStringocument_settingContent() {
        BytesDocument inDoc = MockDocuments.mockEmptyByteDoc()
                .cloneWithContent(DocumentType.MIME_UNKNOWN, Optional.empty(), "old".getBytes());
        
        StringDocument outDoc = underTest.toStringDocument(inDoc, DocumentType.MIME_HTML_TEXT, "new");
        
        Assert.assertEquals("Check the content was replaced.", "new", outDoc.getContentAsString());
        Assert.assertEquals("Check the document type was updated.", DocumentType.MIME_HTML_TEXT, outDoc.getDocumentType());
        Assert.assertTrue("All string documents have a charset.", outDoc.getCharset().isPresent());
    }
}
