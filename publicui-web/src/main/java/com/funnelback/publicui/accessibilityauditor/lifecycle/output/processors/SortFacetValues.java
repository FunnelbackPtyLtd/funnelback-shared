package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.common.filter.accessibility.Metadata.Names;
import com.funnelback.common.filter.accessibility.metadata.MetdataValueMappers.TechniquesAffectedBy;
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
        valueComparators.put(Metadata.getMetadataClass(Names.explicitFailedLevels().getName()), new LevelNameComparator());
        valueComparators.put(Metadata.getMetadataClass(Names.setOfFailingPrinciples().getName()), new PrincipleIdComparator());
        valueComparators.put(Metadata.getMetadataClass(Names.techniquesAffectedBy().getName()), new AffectedByComparator());
        valueComparators.put(Metadata.getMetadataClass(Names.setOfFailingSuccessCriterions().getName()), new SuccessCriterionNameComparator());
        valueComparators.put(Metadata.getMetadataClass(Names.setOfFailingTechniques().getName()), new TechniqueNameComparator());
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
     * Compare AA Success Criteria by their label (e.g. : 1.4.6 - Contrast (Enhanced))
     */
    static class SuccessCriterionNameComparator implements Comparator<CategoryValue> {
        @Override
        public int compare(CategoryValue a, CategoryValue b) {
            return a.getLabel().compareTo(b.getLabel());
        }
    }
    
    /**
     * Compare AA Techniques by their label
     */
    static class TechniqueNameComparator implements Comparator<CategoryValue> {
        @Override
        public int compare(CategoryValue a, CategoryValue b) {
            return a.getLabel().compareTo(b.getLabel());
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
        private static final TechniquesAffectedBy TECHNIQUES_AFFECTED_BY = new TechniquesAffectedBy();

        @Override
        public int compare(CategoryValue a, CategoryValue b) {
            int orderA = TECHNIQUES_AFFECTED_BY.fromIndexForm(a.getData()).map(AffectedBy::ordinal).orElse(-1);
            int orderB = TECHNIQUES_AFFECTED_BY.fromIndexForm(b.getData()).map(AffectedBy::ordinal).orElse(-1);

            // Order in enum is least likely to be a failure to most likely 
            // multiply by -1 to switch the order to most likely failure to least likely.
            return Integer.compare(orderA, orderB) * -1;
        }

    }

}
