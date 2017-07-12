package com.funnelback.publicui.contentauditor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.Getter;
import lombok.Setter;

/**
 * A custom metadata field fill which is returns only categories with a count greater than the given threshold.
 * 
 * Currently used only by Content Auditor to show duplicates (i.e. count greater than one)
 *  
 *  @since 15.0
 */
//@NoArgsConstructor //Why do we have this
public class CountThresholdMetadataFieldFill extends MetadataFieldFill {

    /** Only categories with a count greater than this threshold will be included. Default is 1. */
    @Getter @Setter private Integer threshold = 1;
    
    @Override
    public List<CategoryValue> computeValues(final SearchTransaction st, FacetDefinition facetDefinition) {
        // TODO , FacetDefinition facetDefinition
        List<CategoryValue> result = super.computeValues(st, facetDefinition);
        return result.stream().filter((cv) -> Optional.ofNullable(cv.getCount()).orElse(Integer.MIN_VALUE) > threshold).collect(Collectors.toList());
    }

    public CountThresholdMetadataFieldFill(String metaDataClass, Integer threshold) {
        super(metaDataClass);
        this.threshold = threshold;
    }
}
