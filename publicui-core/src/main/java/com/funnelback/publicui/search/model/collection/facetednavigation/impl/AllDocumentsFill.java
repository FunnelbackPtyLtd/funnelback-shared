package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.FacetSearchData;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;


public class AllDocumentsFill extends CategoryDefinition {
    
    private static final String NAME = "AllDocumentsFill";
    
    public AllDocumentsFill(String nameToDisplay) {
        super(nameToDisplay);
    }

    @Override
    public List<CategoryValueComputedDataHolder> computeData(SearchTransaction st, FacetDefinition fdef) {
        List<CategoryValueComputedDataHolder> categories = new ArrayList<>();
        
            
        FacetSearchData facetData = getFacetSearchData(st, fdef);
        
        Integer count = facetData.getResponseForCounts()
            .apply(this, data)
            .map(SearchResponse::getResultPacket)
            .map(ResultPacket::getResultsSummary)
            .map(ResultsSummary::getTotalMatching)
            .orElse(facetData.getCountIfNotPresent().apply(this, data));
        
        categories.add(new CategoryValueComputedDataHolder(data, 
            data, 
            count, 
            "AllDocumentsFill", 
            isSelected(st), 
            getQueryStringParamName(), 
            data));
        
        return categories;
    }
    
    public boolean isSelected(SearchTransaction st) {
        // This is selected when the URL contains the key and value after this value has been selected.
        // this is also selected when this facet has not been selected, as this categoru value matches all
        // documents so no selection is the same as this being selected.
        // In tabs (which this is designed for) it makes sense to assume ALL is selected when the search page
        // is first visited.
        return FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), data)
            || !FacetedNavigationUtils.isFacetSelected(this, st.getQuestion().getSelectedCategoryValues());
    }

    @Override
    public String getQueryStringCategoryExtraPart() {
        return NAME;
    }

    @Override
    public boolean matches(String value, String extraParams) {
        return data.equals(value) && extraParams.equals(NAME);
    }

    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return Collections.emptyList();
    }

}
