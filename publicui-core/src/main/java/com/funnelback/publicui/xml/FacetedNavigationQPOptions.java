package com.funnelback.publicui.xml;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;

/**
 * Generates QP options to run when the given categories are enabled.
 * 
 * <p>The caller is required to give this every category that will be used when the
 * query runs. This will not inspect subCategoroes so the caller must pass in each
 * sub category.</p>
 *
 */
public class FacetedNavigationQPOptions {
        
    private boolean hasGscopeCategory = false;
    private boolean hasDateCategory = false;
    private boolean hasURLItems = false; 
    private Set<String> metadataClassesToCount = new HashSet<>(); //-rmcf=[f,a,c]
    
    public void add(DateFieldFill dateFieldFill) {
        this.hasDateCategory = true;
    }
    
    public void add(GScopeItem gScopeItem) {
        this.hasGscopeCategory = true;
    }
    
    public void add(MetadataFieldFill metadataFieldFill) {
        this.metadataClassesToCount.add(metadataFieldFill.getMetadataClass());
    }
    
    public void add(URLFill urlFill) {
        this.hasURLItems = true;
    }
    
    public String getQPOptions() {
        StringBuilder sb = new StringBuilder();
        if(hasGscopeCategory) {
            sb.append("-countgbits=all ");
        }
        if(hasDateCategory) {
            sb.append("-count_dates=d ");
        }
        if(hasURLItems) {
            sb.append("-count_urls=1000 ");
        }
        if(metadataClassesToCount.size() > 0) {
            sb.append("-rmcf=[");
            sb.append(StringUtils.join(metadataClassesToCount, ","));
            sb.append("] ");
        }
        
        return sb.toString();
    }
    
}
