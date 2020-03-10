package com.funnelback.filter.api.mock;

import com.funnelback.filter.api.DocumentType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;


@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MockDocumentType implements DocumentType {

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
