package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;

import com.funnelback.publicui.search.model.transaction.Facet;

import lombok.RequiredArgsConstructor;

/**
 * Leaves the order of categories as they are, which should be the order 
 * in which they appear in the facet config.
 */
@RequiredArgsConstructor
public class FacetConfigOrder implements Comparator<Facet.CategoryValue> {
    
    @Override
    public int compare(Facet.CategoryValue c1, Facet.CategoryValue c2) {
        return 0;
    }

}