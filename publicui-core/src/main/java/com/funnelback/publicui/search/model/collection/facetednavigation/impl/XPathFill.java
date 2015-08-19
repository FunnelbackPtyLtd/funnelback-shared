package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>{@link CategoryDefinition} filled by an XPath expression.</p>
 * 
 * <p>Each different value of the XPath expression will generate a category.</p>
 * 
 * <p>Values are stored in an automatically assigned metadata class.</p>
 * 
 * @since 11.0
 */
public class XPathFill extends CategoryDefinition implements MetadataBasedCategory {
    
    /**
     * <p>Automatically assigned metadata class for the values
     * of the XPath expression.</p>
     */
    @Getter @Setter private String metafield;
    
    /** {@inheritDoc} */
    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    public List<CategoryValue> computeValues(final SearchTransaction st) {
        List<CategoryValue> categories = new ArrayList<CategoryValue>();
        
        // For each metadata count <rmc item="a:new south wales">42</rmc>
        for (Entry<String, Integer> entry: st.getResponse().getResultPacket().getRmcs().entrySet()) {
            String item = entry.getKey();
            if (item.startsWith(MetadataBasedCategory.METADATA_ABSENT_PREFIX)) {
                continue; // Skip the 'documents with none of this metadata' count
            }
            int count = entry.getValue();
            MetadataAndValue mdv = parseMetadata(item);
            
            // If the automatically assigned metafield matches
            if (this.metafield.equals(mdv.metadata)) {
                categories.add(new CategoryValue(
                        mdv.value,
                        mdv.value,
                        count,
                        URLEncoder.encode(getQueryStringParamName(), "UTF-8")
                            + "=" + URLEncoder.encode(mdv.value, "UTF-8"),
                        getMetadataClass()));
            }
        }
        return categories;
    }

    /** {@inheritDoc} */
    @Override
    public String getQueryStringParamName() {
        return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + metafield;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean matches(String value, String extraParams) {
        return metafield.equals(extraParams);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getMetadataClass() {
        return metafield;
    }

    /** {@inheritDoc} */
    @Override
    public String getQueryConstraint(String value) {
        return metafield + ":\"" + MetadataBasedCategory.INDEX_FIELD_BOUNDARY + " "
                + value
                + " " + MetadataBasedCategory.INDEX_FIELD_BOUNDARY + "\"";
    }
}
