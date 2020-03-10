package com.funnelback.filter.api.mock;

import com.funnelback.filter.api.DocumentType;
import com.funnelback.filter.api.DocumentTypeFactory;

public class UnknownDocumentTypeFactory implements DocumentTypeFactory {

    @Override
    public DocumentType fromContentTypeHeader(String contentTypeHeader) {
        return DocumentType.MIME_UNKNOWN;
    }

}
