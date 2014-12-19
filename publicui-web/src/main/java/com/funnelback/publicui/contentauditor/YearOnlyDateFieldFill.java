package com.funnelback.publicui.contentauditor;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A date facet which keeps only the year categories (e.g. 2014), not the
 * relative 'Last X months' ones which are confusing in content auditor because
 * they overlap.
 */
public class YearOnlyDateFieldFill extends DateFieldFill {

    @Override
    public List<CategoryValue> computeValues(final SearchTransaction st) {
        List<CategoryValue> result = new ArrayList<>();
        
        for (CategoryValue cv : super.computeValues(st)) {
            if (cv.getLabel().matches("\\d+")) {
                result.add(cv);
            }
        }
        
        return result;
    }
}
