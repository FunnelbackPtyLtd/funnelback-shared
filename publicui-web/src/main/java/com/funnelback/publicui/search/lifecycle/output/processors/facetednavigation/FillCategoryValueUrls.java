package com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.function.Flattener;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.utils.QueryStringUtils;
import com.google.common.collect.ImmutableList;

public class FillCategoryValueUrls {
    
    

    /**
     * Adds the select, unselect and toggle URL to the given category.
     *  
     * <p>URLs can only be filled out once we have gathered all of the data for 
     * the sub categories. When this called the given Category should have its sub categories
     * set although they do not need to have the URL set.</p>
     * 
     * @param cDef
     * @param st
     * @param category
     */
    public void fillURLs(SearchTransaction st, 
            FacetDefinition facetDef,
            List<Facet.Category> siblings,
            Facet.Category category) {
        for(CategoryValue categoryValue : category.getValues()) {
            
            String selectUrl = QueryStringUtils.toString(getSelectUrlMap(st, facetDef, categoryValue, siblings), true);
            String unselectUrl = QueryStringUtils.toString(getUnselectUrlMap(st, category, categoryValue), true);
            
            String toggleUrl = categoryValue.isSelected()? unselectUrl : selectUrl;
            
            categoryValue.setSelectUrl(selectUrl);
            categoryValue.setUnselectUrl(unselectUrl);
            categoryValue.setToggleUrl(toggleUrl);
        }
    }
    
    /**
     * Creates the URL paramaters for selecting the given category value.
     * 
     * <p>I am pretty sure this will not work when multiple category types are mixed under the same facet
     * definition. Perhaps a problem with the stencils also exists?</p>
     * 
     * @param st
     * @param facetDef
     * @param categoryValue
     * @return
     */
    Map<String, List<String>> getSelectUrlMap(SearchTransaction st, FacetDefinition facetDef, CategoryValue categoryValue,
        List<Facet.Category> siblings) {
        // Will this work when selecting in the case of 
        Map<String, List<String>> selectUrlQs = st.getQuestion().getQueryStringMapCopy();
        
        if(facetDef.getSelectionType() == FacetSelectionType.SINGLE) {
            // This will replace any currently selected value of the facet, this is NOT what
            // we want when in "Multiple value mode"
            FacetedNavigationUtils.removeQueryStringFacetKey(selectUrlQs, categoryValue.getQueryStringParamName());
            
            // Also remove any selected siblings 
            // We also remove any selected children of our siblings, I don't think this will happen 
            // but it is safer this way, if someone gets into a bad state we effectivly get them
            // back to a good state.
            siblings.stream()
                .flatMap(Flattener.mapper(c -> c.getCategories()))
                .forEach(cv -> { 
                    FacetedNavigationUtils.removeQueryStringFacetKey(selectUrlQs, cv.getQueryStringParamName());
                });
            
            selectUrlQs.put(categoryValue.getQueryStringParamName(), asList(categoryValue.getQueryStringParamValue()));
        } else {
            // Unselect the URL.
            FacetedNavigationUtils.removeQueryStringFacetValue(selectUrlQs, 
                categoryValue.getQueryStringParamName(), 
                categoryValue.getQueryStringParamValue());
            
            List<String> values = new ArrayList<>();
            
            if(selectUrlQs.containsKey(categoryValue.getQueryStringParamName())) {
               values.addAll(selectUrlQs.get(categoryValue.getQueryStringParamName())); 
            }
            
            values.add(categoryValue.getQueryStringParamValue());
            
            selectUrlQs.put(categoryValue.getQueryStringParamName(), values);
        }
        
        new FillFacetUrls().removeParameters(selectUrlQs);
        
        return selectUrlQs;
    }
    
    Map<String, List<String>> getUnselectUrlMap(SearchTransaction st, Facet.Category category, CategoryValue categoryValue) {
        // Unselect the current facet.
        // This will work in "Multiple value mode" as it only removes the matching facet
        // and not other selected facets ie the value removed must also match.
        Map<String, List<String>> unselectUrlQs = st.getQuestion().getQueryStringMapCopy();
        // Remove specfic key and value from params.
        FacetedNavigationUtils.removeQueryStringFacetValue(unselectUrlQs, 
            categoryValue.getQueryStringParamName(), categoryValue.getQueryStringParamValue());
        
        // Also remove any other query string parameter used by any sub-categories of
        // the current category. This is for hierarchical facets, so that unselecting a parent
        // category also unselect all children
        
        Optional.ofNullable(category.getCategories()).orElse(Collections.emptyList())
            .stream()
            .flatMap(Flattener.mapper(Facet.Category::getCategories))
            .map(subCat -> subCat.getValues())
            .flatMap(List::stream)
            .map(value -> value.getQueryStringParamName())
            // Remove all children.
            .forEach(queryStringParamName -> FacetedNavigationUtils.removeQueryStringFacetKey(unselectUrlQs, queryStringParamName));
        
        new FillFacetUrls().removeParameters(unselectUrlQs);
        
        return unselectUrlQs;
    }
}
