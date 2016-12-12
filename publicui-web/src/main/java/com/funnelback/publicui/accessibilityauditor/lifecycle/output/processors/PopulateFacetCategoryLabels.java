package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.common.filter.accessibility.Metadata.Names;
import com.funnelback.common.function.StreamUtils;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.wcag.checker.AccessibilityChecker;
import com.funnelback.wcag.checker.CheckerClasses;
import com.funnelback.wcag.checker.FailureType;
import com.funnelback.wcag.model.WCAG20Principle;
import com.funnelback.wcag.model.WCAG20SuccessCriterion;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * <p>Populate labels for the values of the accessibility auditor facets</p>
 * 
 * <p>Some facets values use identifiers (e.g. "1.3.1") but we want a human readable facet to
 * display to the user (e.g. "1.3.1 - Info and Relationships") so we process the category values and
 * populate their <code>label</code> (the <code>data</code> field still holds the original
 * value)</p>
 * 
 * @author nguillaumin@funnelback.com
 *
 */
@Log4j2
@Component("accessibilityAuditorPopulateFacetCategoryLabels")
public class PopulateFacetCategoryLabels extends AbstractAccessibilityAuditorOutputProcessor {

    /** Prefix to use to fetch translations for the "affected by" facet */
    private static final String AFFECTED_BY_FACET_I18N_PREFIX = "facets.label.affectedBy.";
    
    @Autowired
    @Setter(AccessLevel.PACKAGE)
    private I18n i18n;
    
    /**
     * <p>Maps facet to process with a function to populate the value labels</p>
     * 
     * <p>Accessibility Auditor Facets are named after the metadata they use, that's why we can use
     * a metadata name as a key here</p>
     */
    private final Map<Metadata, Consumer<CategoryValue>> categoryValuesPopulators = new HashMap<>();

    /**
     * Populate the populator (!) map with the functions for the facets we care about
     */
    public PopulateFacetCategoryLabels() {
        // We need checker instances (not classes) to do checker.problemDescription() later
        Set<AccessibilityChecker> checkers = Arrays.asList(CheckerClasses.allCheckerClasses)
            .stream()
            .map(StreamUtils::newInstance)
            .collect(Collectors.toSet());

        // Populator for the "principles" facet
        categoryValuesPopulators.put(
            Names.principle(),
            value -> {
                try {
                    value.setLabel(WCAG20Principle.fromSection(Integer.parseInt(value.getData())).id);
                } catch (NumberFormatException nfe) {
                    log.warn("Unexpected WCAG20Principle section: '{}'", value.getData());
                }
            });
        
        // Populator for the "success criterion" facet
        categoryValuesPopulators.put(
            Names.successCriterion(),
            value -> {
                try {
                    value.setLabel(value.getData() + " - " + WCAG20SuccessCriterion.fromSection(value.getData()).title);
                } catch (IllegalArgumentException iae) {
                    log.warn("Unexpected success criterion section: '{}'", value.getData());
                }
            });
        
        categoryValuesPopulators.put(
            Names.affectedBy(),
            value -> {
                value.setLabel(i18n.tr(AFFECTED_BY_FACET_I18N_PREFIX + value.getData()));
            });
            
        for (FailureType type : FailureType.values()) {
            // Populator for the "issue types" (checker class) facet
            categoryValuesPopulators.put(
                Names.issueTypes(type),
                value -> {
                    // Find corresponding checker instance
                    Optional<AccessibilityChecker> checker = checkers.stream()
                        .filter(accessibilityChecker -> accessibilityChecker.getClass().getSimpleName().equals(value.getData()))
                        .findFirst();

                    if (checker.isPresent()) {
                        value.setLabel(checker.get().getProblemDescription());
                    } else {
                        log.warn("Unexpected checker class name: '{}'", value.getData());
                    }
                });
        }
    }

    @Override
    protected void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws OutputProcessorException {
        if (transaction.hasResponse()) {
            transaction.getResponse()
                .getFacets()
                .forEach(facet -> facet.getCategories()
                    // Note: This is not recursive, but we know AA facets only have 1 level
                    .forEach(category -> populateCategoryLabels(facet, category)));
        }

    }

    /**
     * Populate the category value labels for a given facet and category
     * 
     * @param facet Facet the category belong to
     * @param category Category whose values should be populated
     */
    private void populateCategoryLabels(Facet facet, Category category) {
        Optional<Metadata> mapKeyOption = this.categoryValuesPopulators.keySet()
            .stream()
            .filter(key -> Metadata.getMetadataClass(key.getName()).equals(facet.getName()))
            .findFirst();

        if (mapKeyOption.isPresent()) {
            // We have a populator for this facet, apply it
            Consumer<CategoryValue> populator = categoryValuesPopulators.get(mapKeyOption.get());
            category.getValues().forEach(populator);
        }
    }

}
