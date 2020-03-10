package com.funnelback.filter.api.mock;

import java.util.Optional;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.documents.FilterableDocument;
import com.funnelback.filter.api.documents.StringDocument;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * A String document for testing.
 *
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MockStringDocument implements StringDocument {

    @NonNull @Getter private final URI URI;
    @NonNull @Getter private final ImmutableListMultimap<String, String> metadata;
    @NonNull @Getter private final String contentAsString;
    @NonNull @Getter private final DocumentType documentType;
    
    /**
     * Constructs a StringDocument with the given content.
     * 
     * @param URI
     * @param metadata
     * @param content
     */
    public MockStringDocument(URI URI, 
                                    Multimap<String, String> metadata,
                                    DocumentType documentType,
                                    @NonNull String content) {
        this.URI = URI;
        this.metadata = ImmutableListMultimap.copyOf(metadata);
        this.contentAsString = content;
        this.documentType = documentType;
    }
    

    @Override
    public byte[] getCopyOfContents() {
        return contentAsString.getBytes(getCharset().get());
    }
    
    /**
     * Charset is always UTF_8 when in string form, the bytes returned will be UTF_8
     */
    @Override
    public Optional<Charset> getCharset() {
        return Optional.of(StandardCharsets.UTF_8);
    }
    
    @Override
    public ListMultimap<String, String> getCopyOfMetadata() {
        return ArrayListMultimap.create(this.metadata);
    }

    @Override
    public StringDocument cloneWithURI(URI uri) {
        return new MockStringDocument(uri, metadata, documentType, contentAsString);
    }

    @Override
    public StringDocument cloneWithMetadata(ListMultimap<String, String> metadata) {
        return new MockStringDocument(URI, metadata, documentType, contentAsString);
    }
    
    @Override
    public StringDocument cloneWithStringContent(DocumentType documentType, String content) {
        return new MockStringDocument(URI, metadata, documentType, content);
    }
    
    @Override
    public FilterableDocument cloneWithDocumentType(DocumentType documentType) {
        return new MockStringDocument(URI, metadata, contentAsString, documentType);
    }
}
