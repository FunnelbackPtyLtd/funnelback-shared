package com.funnelback.publicui.contentauditor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.SneakyThrows;

import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.MetadataAndValue;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

/**
 * A custom metadata fill which is used to capture the 'remainder' documents (i.e. those which did not have any of the 
 * desired type of metadata).
 * 
 * Note that the query which will be run by selecting this relies on -ifb having been used as an indexer option.
 */
public class MetadataMissingFill extends MetadataFieldFill {
    
    /** The category label shown for the remainder facet */
    private static final String CATEGORY_LABEL = "None";
    
    /** Special value shown as the param value (though this is not actually used) */
    private static final String UNSET_VALUE = "$unset";
    
    /**
     * Works just like MetadataFieldFill's setData, but always prefixes a '-'
     * to distinguish it from the MetadataFieldFill one.
     */
    @Override
    public void setData(String data) {
        this.data = "-" + data;
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
            int remainderCount = st.getResponse().getResultPacket().getResultsSummary().getTotalMatching();
            
            // Subtract out the metadata entries which have been assigned
            for (Entry<String, Integer> entry : st.getResponse().getResultPacket().getRmcs().entrySet()) {
                String item = entry.getKey();
                int count = entry.getValue();
                MetadataAndValue mdv = parseMetadata(item);
                if (this.data.equals("-" + mdv.metadata)) {
                    remainderCount -= count;
                }
            }
    
            if (remainderCount > 0) {
                // There are some documents which didn't have any matches, so add a new remainder entry
                categories.add(new CategoryValue(
                    CATEGORY_LABEL,
                    CATEGORY_LABEL,
                    remainderCount,
                    URLEncoder.encode(getQueryStringParamName(), "UTF-8")
                    + "=" + URLEncoder.encode(UNSET_VALUE, "UTF-8"),
                    getMetadataClass()));
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
        return data + ":" + MetadataBasedCategory.INDEX_FIELD_BOUNDARY;
    }
}
