package com.funnelback.filter.api.mock;

import java.util.Optional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.documents.BytesDocument;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * A BytesDocument for testing.
 *
 */
@EqualsAndHashCode
@ToString
public class MockBytesDocument implements BytesDocument {

    @Getter @NonNull private final URI URI;
    @Getter @NonNull private final ImmutableListMultimap<String, String> metadata;
    @Getter @NonNull private byte[] content;
    @Getter @NonNull private final Optional<Charset> charset;
    @Getter @NonNull private final DocumentType documentType;
    
    /**
     * Use {@link MockDocuments#mockByteDoc} instead.
     * 
     * @param URI
     * @param metadata
     * @param documentType
     * @param charset
     * @param content
     */
    MockBytesDocument(URI URI, 
                            ListMultimap<String, String> metadata,
                            DocumentType documentType,
                            Optional<Charset> charset,
                            byte[] content) {
        this.URI = URI;
        this.metadata = ImmutableListMultimap.copyOf(metadata);
        this.content = content;
        this.charset = charset;
        this.documentType = documentType;
    }
    
    @Override
    public byte[] getCopyOfContents() {
        return content.clone();
    }
    
    @Override
    public ListMultimap<String, String> getCopyOfMetadata() {
        return ArrayListMultimap.create(this.metadata);
    }

    @Override
    public MockBytesDocument cloneWithURI(URI uri) {
        return new MockBytesDocument(uri, metadata, documentType, charset, content);
    }

    @Override
    public MockBytesDocument cloneWithMetadata(ListMultimap<String, String> metadata) {
        return new MockBytesDocument(URI, metadata, documentType, charset, content);
    }

    @Override
    public MockBytesDocument cloneWithContent(DocumentType documentType, Optional<Charset> charset, byte[] content) {
        return new MockBytesDocument(URI, metadata, documentType, charset, content);
    }

    @Override
    public InputStream contentAsInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public MockBytesDocument cloneWithDocumentType(DocumentType documentType) {
        return new MockBytesDocument(URI, metadata, documentType, charset, content);
    }
}
