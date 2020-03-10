package com.funnelback.filter.api.mock;

import java.util.Optional;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.FilterDocumentFactory;
import com.funnelback.filter.api.documents.BytesDocument;
import com.funnelback.filter.api.documents.FilterableDocument;
import com.funnelback.filter.api.documents.StringDocument;

/**
 * A FilterDocumentFactory that can used in testing.
 * 
 * The FilterDocumentFactory that is provided under Funnelback e.g. during a crawl
 * is slightly different to this. The Funnelback provided one is both more efficient
 * and capable of guessing the charset, if not already set on the FilterableDocument. 
 *
 */
public class MockFilterDocumentFactory implements FilterDocumentFactory {

    @Override
    public BytesDocument toBytesDocument(FilterableDocument filterableDocument) {
        return new MockBytesDocument(filterableDocument.getURI(), 
            filterableDocument.getMetadata(), 
            filterableDocument.getDocumentType(), 
            filterableDocument.getCharset(), 
            filterableDocument.getCopyOfContents());
    }

    @Override
    public Optional<StringDocument> toStringDocument(FilterableDocument filterableDocument) {
        if(filterableDocument.getCharset().isPresent()) {
            return Optional.of(new MockStringDocument(filterableDocument.getURI(),
                filterableDocument.getMetadata(),
                filterableDocument.getDocumentType(),
                new String(filterableDocument.getCopyOfContents(), filterableDocument.getCharset().get())));
        }
        return Optional.empty();
    }

    @Override
    public StringDocument toStringDocument(FilterableDocument filterableDocument, 
                                                DocumentType documentType,
                                                String content) {
        return new MockStringDocument(filterableDocument.getURI(), 
            filterableDocument.getMetadata(),
            documentType,
            content);
    }

}
