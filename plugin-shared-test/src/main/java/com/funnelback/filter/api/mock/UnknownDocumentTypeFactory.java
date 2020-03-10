package com.funnelback.filter.api.mock;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.DocumentTypeFactory;

/**
 * A document type factory which always returns {@link DocumentType#MIME_UNKNOWN}.
 * 
 * The {@link DocumentTypeFactory} that is provided under Funnelback e.g. during a crawl
 * will provide some support for creating a DocumentType from the Content-Type HTTP header.  
 *
 */
public class UnknownDocumentTypeFactory implements DocumentTypeFactory {

    @Override
    public DocumentType fromContentTypeHeader(String contentTypeHeader) {
        return DocumentType.MIME_UNKNOWN;
    }

}
