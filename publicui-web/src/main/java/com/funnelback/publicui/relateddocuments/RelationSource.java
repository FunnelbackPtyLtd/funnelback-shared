package com.funnelback.publicui.relateddocuments;

import java.util.Set;

import com.funnelback.publicui.search.model.padre.Result;

/**
 * An interface representing the source of a set of URLs for document
 * related to the given result.
 * 
 * Implementation can determine how to extract the related URLs
 * from the result in whatever way is required.
 */
public interface RelationSource {
    public Set<String> getValuesForResult(Result result);
}
