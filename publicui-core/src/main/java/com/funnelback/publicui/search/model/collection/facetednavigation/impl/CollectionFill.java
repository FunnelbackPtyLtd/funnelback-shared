package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.Arrays;
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
import com.funnelback.publicui.search.model.facetednavigation.FacetSelectedDetailts;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.google.common.collect.ImmutableList;

import lombok.Getter;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_UNSCOPED_ALL_QUERY;
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
        // Padre returns zero result values this is not like gscopes, rmcf, dates, URL drill down
        // so filter out zero result values for consistency.
        List<String> collectionsListed = facetSearchData.getResponseForValues()
                                                .getResultPacket()
                                                .getDocumentsPerCollection()
                                                .entrySet()
                                                .stream()
                                                .filter(e -> e.getValue() > 0L)
                                                .map(e -> e.getKey())
                                                .filter(Predicates.containedBy(collections))
                                                .collect(Collectors.toList());
        
        
        // FROM_UNSCOPED_ALL_QUERY is special in that made we always want to return a value
        // This mode is usually used in tabs so we want a consistent set values being returned.
        if(collectionsListed.isEmpty() && fdef.getFacetValues() != FROM_UNSCOPED_ALL_QUERY) {
            Optional<FacetSelectedDetailts> selectedDetails = 
               getMatchingFacetSelectedDetails(st.getQuestion()).stream().findFirst();
            if(selectedDetails.isPresent()) {
                return Arrays.asList(makeCategoryValue(0, true));
            }
            //Nothing knows about any of the collections we have.
            return Collections.emptyList();
        }
        
        Long count = null;
        
        Optional<SearchResponse> searchResponseForCounts = facetSearchData.getResponseForCounts().apply(this, value);
        
        if(searchResponseForCounts.isPresent()) {
            count = 0L;
            for(String collection : collectionsListed) {
                Long countFromColl = searchResponseForCounts
                    .map(SearchResponse::getResultPacket)
                    .map(ResultPacket::getDocumentsPerCollection)
                    .map(docsPerColl -> docsPerColl.get(collection))
                    .orElse(null);
                
                count += countFromColl;
            }
            
        } else {
            count = Optional.ofNullable(facetSearchData.getCountIfNotPresent().apply(this, value))
                .map(i -> (long) i)
                .orElse(null);
        }
        
        return Arrays.asList(makeCategoryValue(
                Optional.ofNullable(count).map(Long::intValue).orElse(null),
                FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), data)));
    }
    
    private CategoryValueComputedDataHolder makeCategoryValue(Integer count, boolean selected) {
        return new CategoryValueComputedDataHolder(queryStringCategoryExtraPart, 
            data, 
            count, 
            "",
            selected,
            getQueryStringParamName(),
            data);
    }

    @Override
    public boolean matches(String value, String extraParams) {
        return data.equals(value) && queryStringCategoryExtraPart.equals(extraParams);
    }

    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return ImmutableList.of(new QueryProcessorOption<Boolean>(QueryProcessorOptionKeys.DOCS_PER_COLLECTION, true));
    }

    @Override
    public boolean allValuesDefinedByUser() {
        return true;
    }

    @Override
    public boolean selectedValuesAreNested() {
        return false;
    }

}
