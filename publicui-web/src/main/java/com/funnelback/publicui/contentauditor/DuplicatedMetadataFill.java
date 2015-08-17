package com.funnelback.publicui.contentauditor;

import java.util.List;
import java.util.stream.Collectors;

import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A custom metadata fill which is returns only categories with a count greater than one (i.e. duplicates)
 */
public class DuplicatedMetadataFill extends MetadataFieldFill {

    @Override
    public List<CategoryValue> computeValues(final SearchTransaction st) {
        List<CategoryValue> result = super.computeValues(st);
        return result.stream().filter((cv) -> cv.getCount() > 1).collect(Collectors.toList());
    }
}
