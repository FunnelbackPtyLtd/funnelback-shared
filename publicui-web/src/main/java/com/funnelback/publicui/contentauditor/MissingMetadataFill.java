package com.funnelback.publicui.contentauditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.funnelback.common.padre.MetadataClass;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

/**
 * A custom metadata fill which is used to capture the 'remainder' documents (i.e. those which did not have any of the 
 * desired type of metadata).
 * 
 * Note that the query which will be run by selecting this relies on -ifb having been used as an indexer option.
 * 
 * @since 15.0
 */
public class MissingMetadataFill extends MetadataFieldFill {
    
    /** Value for use as the MetadataFieldFill's 'data' */
    private static final String MISSING_DATA_VALUE = "missing";

    public MissingMetadataFill() {
        super(MissingMetadataFill.MISSING_DATA_VALUE);
    }
    
    /** 
     * Produces only one categoryValue, which will have a count of the remaining documents
     * (i.e. those for which no metadata in this class was set) which will select those documents.
     */
    @Override
    public List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition facetDefinition) {
        List<CategoryValueComputedDataHolder> categories = new ArrayList<>();
        
        if (st.hasResponse() && st.getResponse().hasResultPacket() && st.getResponse().getResultPacket().hasResults()) {
            FacetSearchData facetData = getFacetSearchData(st, facetDefinition);
            
            // Subtract out the metadata entries which have been assigned
            for (Entry<String, Integer> entry : facetData.getResponseForValues().getResultPacket().getRmcs().entrySet()) {
                String item = entry.getKey();
                if (!item.startsWith(MetadataBasedCategory.METADATA_ABSENT_PREFIX)) {
                    continue; // Skip the actual entry counts
                }
                
                MetadataAndValue mdv = parseMetadata(item);
                
                // Strip absent prefix
                String metadataClass = mdv.metadata.substring(MetadataBasedCategory.METADATA_ABSENT_PREFIX.length());
                
                Integer count = facetData.getResponseForCounts().apply(this, metadataClass)
                        .map(SearchResponse::getResultPacket)
                        .map(ResultPacket::getRmcs)
                        .map(rmcs -> rmcs.get(entry.getKey()))
                        .orElse(facetData.getCountIfNotPresent().apply(this, metadataClass));

                if (!MetadataClass.RESERVED_CLASS_PATTERN.matcher(metadataClass).matches()) {
                    // Don't report reserved classes (should not be user-visible)
                    
                    categories.add(new CategoryValueComputedDataHolder(
                        metadataClass,
                        metadataClass,
                        count,
                        getMetadataClass(),
                        FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), metadataClass),
                        getQueryStringParamName(),
                        metadataClass));
                }
            }
        }
        
        return categories;
    }

    /**
    * Produces a query constraint like -x:$++ (i.e. all documents where x was never set to anything).
    * 
    * Value is ignored (but should be expected to be UNSET_VALUE).
    */
    @Override
    public String getQueryConstraint(String value) {
        // This produces a query like -x:$++ (i.e. all documents where x was never set to anything)
        return "-" + value + ":" + MetadataBasedCategory.INDEX_FIELD_BOUNDARY;
    }

    /**
     * Do not return an RMCF option for this type, otherwise we would be counting
     * the number of documents that are missing a metadata named "missing"
     */
    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return Collections.emptyList();
    }

}
