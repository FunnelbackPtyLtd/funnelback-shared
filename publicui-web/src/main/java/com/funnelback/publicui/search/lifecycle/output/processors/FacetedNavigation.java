package com.funnelback.publicui.search.lifecycle.output.processors;

import static com.funnelback.common.function.Predicates.not;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.funnelback.common.Reference;
import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.function.Flattener;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation.CategoryAndSiblings;
import com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation.FillCategoryValueUrls;
import com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation.FillFacetUrls;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.facet.order.ByFirstCategoryValueComparator;
import com.funnelback.publicui.search.model.transaction.facet.order.FacetComparatorProvider;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
/**
 * Transforms result metadata/url/gscope counts into proper facet hierarchy
 */
@Component("facetedNavigationOutputProcessor")
@Log4j2
public class FacetedNavigation extends AbstractOutputProcessor {
    
    @Setter
    private FillCategoryValueUrls fillUrls = new FillCategoryValueUrls();
    
    @Setter
    private FillFacetUrls fillFacetUrls = new FillFacetUrls();
    
    private FacetComparatorProvider comparatorProvider = new FacetComparatorProvider();

    @Override
    public void processOutput(SearchTransaction searchTransaction) {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && SearchTransactionUtils.hasResponse(searchTransaction)
                && searchTransaction.getResponse().hasResultPacket()) {
            
            FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
            
            if (config != null) {
                for(FacetDefinition f: config.getFacetDefinitions()) {
                    AtomicInteger categoryNumber = new AtomicInteger(0);
                    final Facet facet = new Facet(f.getName(), 
                                                    f.getSelectionType(), 
                                                    f.getConstraintJoin(), 
                                                    f.getFacetValues(), 
                                                    f.getOrder());
                    
                    List<Facet.Category> cats = new ArrayList<>();
                    
                    for (final CategoryDefinition ct: f.getCategoryDefinitions()) {
                        Facet.Category c = fillCategories(f, ct, searchTransaction, new AtomicInteger(0));
                        if (c != null) {
                            facet.getCategories().add(c);
                            cats.add(c);
                            if (searchTransaction.getQuestion().getSelectedCategoryValues().containsKey(ct.getQueryStringParamName())
                                && f.getConstraintJoin() == FacetConstraintJoin.LEGACY) {
                                // This category has been selected. Stop calculating other
                                // values for it (FUN-4462)
                                // We only do this in legacy mode, it is not clear if we want to do this in new modes.
                                // we only want to do this in the drill down case.
                                // Note that this does not work for metadata and so this legacy code here makes gscopes
                                // and metadata inconsitent.
                                break;
                            }
                        }
                    }
                    
                    
                     // We can only fill out the URLs after the entire category
                    // with its children and its siblings have been created.
                    List<CategoryAndSiblings> cas = CategoryAndSiblings.toCategoriesWithSiblings(cats);
                    cas.stream().flatMap(Flattener.mapper(c -> {
                        return CategoryAndSiblings.toCategoriesWithSiblings(c.getCategory().getCategories());
                    }))
                    .forEach(catAndSibs -> fillUrls.fillURLs(searchTransaction, f, catAndSibs.getSiblings(), catAndSibs.getCategory()));
                    
                    // Legacy mode has limited support for ordering, we maintain backwards compatibility.
                    
                    // Sort all categories for this facet by the count of the first value of
                    // each category. This is useful for GScope based facets where there's only
                    // one category-value per category
                    // We need to ensure we don't do this if the category has more than one value, otherwise
                    // we can break sorting. See also FUN-10654
                    if(facet.getCategories().stream().allMatch(c -> c.getValues().size() == 1 
                        && c.getCategories().size() == 0)) {
                        Collections.sort(facet.getCategories(), 
                            new ByFirstCategoryValueComparator(
                                comparatorProvider.getComparatorWhenSortingAllValus(f.getOrder(), Optional.ofNullable(facet.getCustomComparator()))));
                    }
                    fillFacetUrls.setUnselectAllUrl(facet, searchTransaction);
                    
                    removeUnslectedValuesForDrillDownFacets(facet);
                    
                    searchTransaction.getResponse().getFacets().add(facet);
                    
                }
            }
        }
    }
    
    public void removeUnslectedValuesForDrillDownFacets(Facet facet) {
        if(facet.getFacetValues() != FacetValues.FROM_SCOPED_QUERY_HIDE_UNSELECTED_PARENT_VALUES) return;
        removeUnslectedValuesForDrillDownFacets(facet.getCategories());
    }
    
    public void removeUnslectedValuesForDrillDownFacets(List<Category> categories) {
        if(categories.stream().map(Category::getValues).flatMap(List::stream).anyMatch(CategoryValue::isSelected)) {
            for(Category c : categories) {
                c.getValues().removeIf(not(CategoryValue::isSelected));
            }
        }
        categories.stream().map(Category::getCategories).forEach(this::removeUnslectedValuesForDrillDownFacets);
    }
    
    
    public Pair<Category, List<CategoryValue>> fillCategory(FacetDefinition facetDefinition, 
                                    CategoryDefinition cDef, 
                                    SearchTransaction searchTransaction,
                                    AtomicInteger depth) {
        // Fill the values for this category
        Category category = new Category(cDef.getLabel(), cDef.getQueryStringParamName());
        
        Reference<Category> parentCat = new Reference<>();
        Reference<Category> categoryToAddValuesTo = new Reference<>(category);
        
        
        List<CategoryValue> allValues = cDef.computeValues(searchTransaction, facetDefinition)
            .stream()
            .peek(c -> {
                categoryToAddValuesTo.getValue().getValues().add(c);
                if(cDef.selectedValuesAreNested() && c.isSelected()) {
                    parentCat.setValue(categoryToAddValuesTo.getValue());
                    c.setCategoryDepth(depth.getAndIncrement());
                    // For the old template we make some effort to sort the values in the desired way.
                    Collections.sort(categoryToAddValuesTo.getValue().getValues(), 
                        comparatorProvider.getComparatorWhenSortingValuesFromSingleCategory(facetDefinition.getOrder()));
                    // Make a new category for the values under this selected value. We do this
                    // so that nesting is like other categories.
                    Category childCat = new Category(cDef.getLabel(), cDef.getQueryStringParamName());
                    categoryToAddValuesTo.getValue().getCategories().add(childCat);
                    categoryToAddValuesTo.setValue(childCat);
                } else {
                    c.setCategoryDepth(depth.get());
                }
            })
            .collect(Collectors.toList());
        
        // For the old template we make some effort to sort the values in the desired way.
        Collections.sort(categoryToAddValuesTo.getValue().getValues(), 
            comparatorProvider.getComparatorWhenSortingValuesFromSingleCategory(facetDefinition.getOrder()));
        
        if(parentCat.getValue() != null) {
            //Ensure that this leaf category has values otherwise we don't want it.
            if(categoryToAddValuesTo.getValue().getValues().isEmpty()) {
                // leaf category has no values remove it
                parentCat.getValue().getCategories().removeIf(c -> c == categoryToAddValuesTo.getValue());
            }
        }
        return Pair.of(category, allValues);
    }
    
    /**
     * Fills the categories for a given {@link CategoryDefinition}, by computing all the
     * values from the result packet (Delegated to the {@link CategoryDefinition} implementation.
     * 
     * If this category has been selected by the user, fills the sub-categories.
     * 
     * @param ct
     * @param searchTransaction
     * @return
     */
    public Facet.Category fillCategories(final FacetDefinition facetDefinition,
            final CategoryDefinition cDef, SearchTransaction searchTransaction,
            AtomicInteger depth) {
        log.trace("Filling category '" + cDef + "'");
        
        Pair<Category, List<CategoryValue>> categoryAndAllValues = fillCategory(facetDefinition, cDef, searchTransaction, depth);
        
        Category category = categoryAndAllValues.getLeft();
        List<CategoryValue> allValues = categoryAndAllValues.getRight();
        
    
        // Find out if this category is currently selected
        if (allValues.stream().anyMatch(CategoryValue::isSelected)) {
            log.trace("Value has been selected.");
            for (CategoryDefinition subCategoryDef: cDef.getSubCategories()) {
                depth.incrementAndGet();
                Facet.Category c = fillCategories(facetDefinition, subCategoryDef, searchTransaction, depth);
                if (c != null) {
                    category.getCategories().add(c);
                    if (searchTransaction.getQuestion().getSelectedCategoryValues().containsKey(subCategoryDef.getQueryStringParamName())
                            && facetDefinition.getConstraintJoin() == FacetConstraintJoin.LEGACY) {
                        // This category has been selected. Stop calculating other
                        // values for it (FUN-4462)
                        // We only do this in legacy mode, it is not clear if we want to do this in new modes.
                        // we only want to do this in the drill down case.
                        // Note that this does not work for metadata and so this legacy code here makes gscopes
                        // and metadata inconsitent.
                        break;
                    }
                }
            }
        }
        
        if (category.hasValues() || category.getCategories().size() > 0) {
            return category;
        } else {
            // No value and no nested category. This can happen for gscope facets.
            // No need to return it.
            return null;
        }
    }

}
