package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.funnelback.common.function.Predicates;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.google.common.collect.ImmutableList;

import lombok.Getter;

public class CollectionFill extends CategoryDefinition {
    
    @Getter private List<String> collections;
    
    @Getter private String queryStringCategoryExtraPart;

    public CollectionFill(String categoryName, List<String> collections) {
        super(categoryName);
        this.collections = ImmutableList.copyOf(collections);
        this.queryStringCategoryExtraPart = StringUtils.join(collections, ",");
    }

    @Override
    public List<CategoryValueComputedDataHolder> computeData(SearchTransaction st, FacetDefinition fdef) {
        FacetSearchData facetSearchData = getFacetSearchData(st, fdef);
        
        String value = queryStringCategoryExtraPart;
        
        // Get the collections from the search that supplies the values.
        List<String> collectionsListed = facetSearchData.getResponseForValues()
                                                .getResultPacket()
                                                .getDocumentsPerCollection()
                                                .keySet()
                                                .stream()
                                                .filter(Predicates.containedBy(collections))
                                                .collect(Collectors.toList());
        
        Long count = null;
        for(String collection : collectionsListed) {
            Long countFromColl = facetSearchData.getResponseForCounts().apply(this, value)
                .map(SearchResponse::getResultPacket)
                .map(ResultPacket::getDocumentsPerCollection)
                .map(docsPerColl -> docsPerColl.get(collection))
                .orElse(null);
            
            
            if(countFromColl != null) {
                if(count == null) {
                    count = 0L;
                }
                count += countFromColl;
            }
        }
        
        if(count == null) {
            count = Optional.ofNullable(facetSearchData.getCountIfNotPresent().apply(this, value))
                    .map(i -> (long) i)
                    .orElse(null);
        }
        
        
        if(count == null) {
            return Collections.emptyList();
        }
        
        return ImmutableList.of(new CategoryValueComputedDataHolder(value, 
            data, 
            count.intValue(), 
            "",
            FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), data),
            getQueryStringParamName(),
            data));
    }

    @Override
    public boolean matches(String value, String extraParams) {
        return data.equals(value) && queryStringCategoryExtraPart.equals(extraParams);
    }

    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return ImmutableList.of(new QueryProcessorOption<Boolean>(QueryProcessorOptionKeys.DOCS_PER_COLLECTION, true));
    }

}
