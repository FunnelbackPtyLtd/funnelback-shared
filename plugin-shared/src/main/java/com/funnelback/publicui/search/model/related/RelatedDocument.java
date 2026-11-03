package com.funnelback.publicui.search.model.related;

import lombok.Data;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
public class RelatedDocument {
    private final URI url;
    private final Map<String, List<String>> metadata;
}