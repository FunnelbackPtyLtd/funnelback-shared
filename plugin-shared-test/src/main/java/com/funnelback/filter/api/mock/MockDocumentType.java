package com.funnelback.filter.api.mock;

import com.funnelback.filter.api.DocumentType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;


/**
 * Use this to create a DocumentType for testing when a preexisting DocumentType on
 * the interface {@link DocumentType} doesn't already exist.
 * 
 * Example creating a DocumentType that claims to be HTML with a specific value for
 * what is returned by {@link DocumentType#asContentType()}. 
 * <code>
 * new MockDocumentType().withHTML(true).withContentType("something/specific");
 * </code>
 * 
 */
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MockDocumentType implements DocumentType {

    /**
     * Construct a DocumentType where the cotent type is unknown.
     * 
     */
    public MockDocumentType() {
        this(false, false, false, DocumentType.MIME_UNKNOWN.asContentType());
    }
    
    @Getter @With public boolean HTML;
    @Getter @With public boolean XML;
    @Getter @With public boolean JSON;
    @With public String contentType;

    @Override
    public String asContentType() {
        return contentType;
    }
}
