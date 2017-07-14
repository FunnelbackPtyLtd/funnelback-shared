package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

/**
 * <p>{@link CategoryDefinition} based on a metadata class.</p>
 * 
 * <p>Will generate multiple values for each value of this metadata class.</p>
 * 
 * @since 11.0
 */
public class MetadataFieldFill extends CategoryDefinition implements MetadataBasedCategory {

    private final List<QueryProcessorOption<?>> qpOptions;
    
    public MetadataFieldFill(String metaDataClass) {
        super(metaDataClass);
        qpOptions = Collections.singletonList(new QueryProcessorOption<String>(QueryProcessorOptionKeys.RMCF, getMetadataClass()));
    }

    /** {@inheritDoc} */
    @Override
    public List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition facetDefinition) {
        List<CategoryValueComputedDataHolder> categories = new ArrayList<>();
        
        FacetSearchData facetData = getFacetSearchData(st, facetDefinition);
        
        // For each metadata count <rmc item="a:new south wales">42</rmc>
        for (Entry<String, Integer> entry : facetData.getResponseForValues().getResultPacket().getRmcs().entrySet()) {
            String item = entry.getKey();
            if (item.startsWith(MetadataBasedCategory.METADATA_ABSENT_PREFIX)) {
                continue; // Skip the 'documents with none of this metadata' count
            }
            
            MetadataAndValue mdv = parseMetadata(item);
            
            Integer count = facetData.getResponseForCounts().apply(this, mdv.value)
                    .map(SearchResponse::getResultPacket)
                    .map(ResultPacket::getRmcs)
                    .map(rmcs -> rmcs.get(entry.getKey()))
                    .orElse(facetData.getCountIfNotPresent().apply(this, mdv.value));
            
            
            if (this.data.equals(mdv.metadata)) {
                String queryStringParamValue = mdv.value;
                categories.add(new CategoryValueComputedDataHolder(
                        mdv.value,
                        mdv.value,
                        count,
                        getMetadataClass(),
                        FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), mdv.value),
                        getQueryStringParamName(),
                        queryStringParamValue
                        )
                    );
            }
        }
        return categories;
    }

    /** {@inheritDoc} */
    @Override
    public String getQueryStringParamName() {
        return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + data;
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

    /** {@inheritDoc} */
    @Override
    public String getQueryConstraint(String value) {
        return data + ":\""+ MetadataBasedCategory.INDEX_FIELD_BOUNDARY + " "
                + value + " "
                + MetadataBasedCategory.INDEX_FIELD_BOUNDARY + "\"";
    }
    
    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return qpOptions;
    }
}
