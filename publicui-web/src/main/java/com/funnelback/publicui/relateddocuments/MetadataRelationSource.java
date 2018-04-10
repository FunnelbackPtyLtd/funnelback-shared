package com.funnelback.publicui.relateddocuments;

import java.util.Set;

import com.funnelback.publicui.search.model.padre.Result;
import com.google.common.collect.Sets;

import lombok.Data;

/**
 * A RelationSource which extracts related URLs from a given metadata class in the result.
 */
@Data
public class MetadataRelationSource implements RelationSource {

    private final String metadataClassName;
    
    @Override
    public Set<String> getValuesForResult(Result result) {
        if (result.getMetaData().containsKey(metadataClassName)) {
            return Sets.newHashSet(result.getMetaData().get(metadataClassName));
        } else {
            return Sets.newHashSet();
        }
    }
}