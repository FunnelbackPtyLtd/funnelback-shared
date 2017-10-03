package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import static com.funnelback.publicui.utils.FacetedNavigationUtils.isCategorySelected;

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
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
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
        
       List<String> selectedValues = getMatchingFacetSelectedDetails(st.getQuestion())
            .stream()
            .map(FacetSelectedDetailts::getValue)
            .collect(Collectors.toList());
       
        
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
            
            
            if (this.data.equals(mdv.metadataClass)) {
                selectedValues.remove(mdv.value);
                boolean selected = isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), mdv.value);
                categories.add(makeValue(mdv.value, selected, count));
            }
        }
        
        for(String value : selectedValues) {
            categories.add(makeValue(value, true, 0));
        }
        return categories;
    }
    
    private CategoryValueComputedDataHolder makeValue(String value, boolean selected, Integer count) {
        return new CategoryValueComputedDataHolder(
            value,
            value,
            count,
            getMetadataClass(),
            selected,
            getQueryStringParamName(),
            value);
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

    @Override
    public boolean allValuesDefinedByUser() {
        return false;
    }

    @Override
    public boolean selectedValuesAreNested() {
        return false;
    }
}
