package com.funnelback.publicui.search.model.related;

import java.util.List;
import java.util.Map;

import java.net.URI;

import lombok.Data;

@Data
public class RelatedDocument {
    private final URI url;
    private final Map<String, List<String>> metadata;
}