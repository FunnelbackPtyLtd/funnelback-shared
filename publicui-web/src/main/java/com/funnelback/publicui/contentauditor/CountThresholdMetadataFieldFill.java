package com.funnelback.publicui.contentauditor;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A custom metadata field fill which is returns only categories with a count greater than the given threshold.
 * 
 * Currently used only by Content Auditor to show duplicates (i.e. count greater than one)
 *  
 *  @since 15.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class CountThresholdMetadataFieldFill extends MetadataFieldFill {

    /** Only categories with a count greater than this threshold will be included. Default is 1. */
    @Getter @Setter private Integer threshold = 1;
    
    @Override
    public List<CategoryValue> computeValues(final SearchTransaction st) {
        List<CategoryValue> result = super.computeValues(st);
        return result.stream().filter((cv) -> cv.getCount() > threshold).collect(Collectors.toList());
    }
}
