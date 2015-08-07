package com.funnelback.publicui.contentauditor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.SneakyThrows;

import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A custom metadata fill which is used to capture the 'remainder' documents (i.e. those which did not have any of the 
 * desired type of metadata).
 * 
 * Note that the query which will be run by selecting this relies on -ifb having been used as an indexer option.
 */
public class MissingMetadataFill extends MetadataFieldFill {
    
    public MissingMetadataFill() {
        super();
        this.data = "missing";
    }
    
    /** 
     * Produces only one categoryValue, which will have a count of the remaining documents
     * (i.e. those for which no metadata in this class was set) which will select those documents.
     */
    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    public List<CategoryValue> computeValues(final SearchTransaction st) {
        List<CategoryValue> categories = new ArrayList<CategoryValue>();
        
        if (st.hasResponse() && st.getResponse().hasResultPacket() && st.getResponse().getResultPacket().hasResults()) {
            
            // Subtract out the metadata entries which have been assigned
            for (Entry<String, Integer> entry : st.getResponse().getResultPacket().getRmcs().entrySet()) {
                String item = entry.getKey();
                if (!item.startsWith("-")) {
                    continue; // Skip the actual entry counts
                }
                
                int count = entry.getValue();
                MetadataAndValue mdv = parseMetadata(item);

                if (!mdv.metadata.startsWith("-Fun")) {
                    // Don't report auto-generated ones
                    
                    String metadataClass = mdv.metadata.replaceFirst("^-", "");
                    
                    categories.add(new CategoryValue(
                        metadataClass,
                        metadataClass,
                        count,
                        URLEncoder.encode(getQueryStringParamName(), "UTF-8")
                        + "=" + URLEncoder.encode(metadataClass, "UTF-8"),
                        getMetadataClass()));
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
}
