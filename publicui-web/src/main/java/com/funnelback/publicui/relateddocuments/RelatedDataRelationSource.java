package com.funnelback.publicui.relateddocuments;

import java.util.Set;
import java.util.stream.Collectors;

import com.funnelback.publicui.search.model.padre.Result;
import com.google.common.collect.Sets;

import lombok.Data;

/**
 * A RelationSource which extracts the related URLs from some previously related document's metadata.
 */
@Data
public class RelatedDataRelationSource implements RelationSource {

    private final String relatedDataKey;
    private final String metadataClassName;
    
    @Override
    public Set<String> getValuesForResult(Result result) {
        if (result.getRelatedDocuments().containsKey(relatedDataKey)) {
            return result.getRelatedDocuments().get(relatedDataKey)
                .stream()
                .map((relatedDocument) -> relatedDocument.getMetadata())
                .filter((metadata) -> metadata.containsKey(metadataClassName))
                .map((metadata) -> metadata.get(metadataClassName))
                .filter((value) -> value != null)
                .collect(Collectors.toSet());
        }
        return Sets.newHashSet();
    }
}
