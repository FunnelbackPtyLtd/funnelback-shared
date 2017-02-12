package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.common.filter.accessibility.Metadata.Names;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.wcag.checker.AffectedBy;

import lombok.extern.log4j.Log4j2;

/**
 * <p>Sort some facet values in a defined order.</p>
 * 
 * <p>For instance we always want the levels to be in
 * the A, AA, AAA order rather than sorted by doc. counts</p>
 * 
 * @author nguillaumin@funnelback.com
 *
 */
@Log4j2
@Component("accessibilityAuditorSortFacetValues")
public class SortFacetValues extends AbstractAccessibilityAuditorOutputProcessor {

    /** Maps facet names with the comparator to use to sort their values */
    private final Map<String, Comparator<CategoryValue>> valueComparators = new HashMap<>();
    
    public SortFacetValues() {
        valueComparators.put(Metadata.getMetadataClass(Names.failedLevels().getName()), new LevelNameComparator());
        valueComparators.put(Metadata.getMetadataClass(Names.principle().getName()), new PrincipleIdComparator());
        valueComparators.put(Metadata.getMetadataClass(Names.affectedBy().getName()), new AffectedByComparator());
    }
    
    @Override
    protected void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws OutputProcessorException {
        if (transaction.hasResponse()) {
            transaction.getResponse()
                .getFacets()
                .forEach(facet -> {
                    Comparator<CategoryValue> comparator = valueComparators.get(facet.getName());
                    if (comparator != null && !facet.getCategories().isEmpty()) {
                        // Note: This is not iterating categories nor recursive, but we know AA facets
                        // only have 1 category and 1 level
                        Collections.sort(facet.getCategories().get(0).getValues(), comparator);
                    }
                });
        }
    }
    
    /**
     * Compare AA level values by their data (which is the level name: A, AA, AAA)
     */
    static class LevelNameComparator implements Comparator<CategoryValue> {
        @Override
        public int compare(CategoryValue a, CategoryValue b) {
            return a.getData().compareTo(b.getData());
        }
    }
    
    /**
     * Compare AA principles values by their identifier (corresponding to the order
     * they appear in the spec
     *
     */
    static class PrincipleIdComparator implements Comparator<CategoryValue> {

        @Override
        public int compare(CategoryValue a, CategoryValue b) {
            try {
                // Identifier is numeric, do a numeric comparison
                int aId = Integer.parseInt(a.getData());
                int bId = Integer.parseInt(b.getData());
                return aId - bId;
            } catch (NumberFormatException nfe) {
                log.warn("Invalid AA principle identifier", nfe);
                return 0;
            }
        }
    }
    
    /**
     * Compare AA "affected by" (Failures, Alert, None).
     */
    static class AffectedByComparator implements Comparator<CategoryValue> {
        
        @Override
        public int compare(CategoryValue a, CategoryValue b) {
            try {
                // Rely on the enum order, as values are defined by decreasing priority
                AffectedBy aAffected = AffectedBy.valueOf(a.getData());
                AffectedBy bAffected = AffectedBy.valueOf(b.getData());
            
                return aAffected.compareTo(bAffected);
            } catch (IllegalArgumentException iae) {
                log.warn("Invalid AA affected by", iae);
                return 0;
            }
        }
        
    }

}
