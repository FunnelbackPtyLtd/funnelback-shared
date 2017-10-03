package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.facetednavigation.FacetSelectedDetailts;
import com.funnelback.publicui.search.model.padre.DateCount;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

/**
 * <p>{@link CategoryDefinition} based on a metadata class
 * containing date values.</p>
 * 
 * <p>Will generate multiple values for each value of this metadata class.</p>
 * 
 * @since 12.0
 */

public class DateFieldFill extends CategoryDefinition implements MetadataBasedCategory {

    private final List<QueryProcessorOption<?>> qpOptions;
    
    public DateFieldFill(String metaDataClass) {
        super(metaDataClass);
        qpOptions = Collections.singletonList(new QueryProcessorOption<String>(QueryProcessorOptionKeys.COUNT_DATES, getMetadataClass()));
    }

    /** {@inheritDoc} */
    @Override
    public List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition facetDefinition) {
        List<CategoryValueComputedDataHolder> categories = new ArrayList<>();
        
        FacetSearchData facetData = getFacetSearchData(st, facetDefinition);
        
        List<String> selectedQueryStringValues = getMatchingFacetSelectedDetails(st.getQuestion())
                .stream()
                .map(FacetSelectedDetailts::getValue)
                .collect(Collectors.toList());
        
        // For each metadata count <rmc item="a:new south wales">42</rmc>
        for (Entry<String, DateCount> entry : facetData.getResponseForValues().getResultPacket().getDateCounts().entrySet()) {
            String item = entry.getKey();
            DateCount dc = entry.getValue();
            MetadataAndValue mdv = parseMetadata(item);
            if (this.data.equals(mdv.metadataClass)) {
                String queryStringParamValue = dc.getQueryTerm();
                selectedQueryStringValues.remove(queryStringParamValue);
                Integer count = facetData.getResponseForCounts().apply(this, mdv.value)
                        .map(SearchResponse::getResultPacket)
                        .map(ResultPacket::getDateCounts)
                        .map(dateCounts -> dateCounts.get(entry.getKey()))
                        .map(DateCount::getCount)
                        .orElse(facetData.getCountIfNotPresent().apply(this, mdv.value));
                
                categories.add(new CategoryValueComputedDataHolder(
                        mdv.metadataClass, // Why is this metadata class?
                        mdv.value,
                        count,
                        getMetadataClass(),
                        FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), dc.getQueryTerm()),
                        getQueryStringParamName(),
                        queryStringParamValue));
            }
        }
        for(String selectQueryStringParamValue : selectedQueryStringValues) {
            categories.add(new CategoryValueComputedDataHolder(
                this.data, // Why is this metadata class?
                selectQueryStringParamValue, // This is the best we can do without reverse engineering padre.
                0,
                getMetadataClass(),
                true,
                getQueryStringParamName(),
                selectQueryStringParamValue));
        }
        return categories;
    }

    /** {@inheritDoc} */
    @Override
    public String getQueryStringCategoryExtraPart() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(String value, String extraParams) {
        return data.equals(extraParams);
    }

    /** {@inheritDoc} */
    @Override
    public String getMetadataClass() {
        return data;
    }

    /**
     * The passed-in value is already a date constraint, so return
     * it as-is.
     * 
     * @param value A date constraint, e.g. <code>d&lt;10Jan2015</code>
     */
    @Override
    public String getQueryConstraint(String value) {
        return value;
    }
    
    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return qpOptions;
    }

    @Override
    public boolean allValuesDefinedByUser() {
        return false;
    }

    @Override
    public boolean selectedValuesAreNested() {
        return false;
    }
}
