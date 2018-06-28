package com.funnelback.publicui.search.model.related;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.funnelback.common.config.CollectionId;

import lombok.Data;

@Data
public class RelatedDocument {
    private final URI url;
    private final String collectionId;
    private final Map<String, List<String>> metadata;
}