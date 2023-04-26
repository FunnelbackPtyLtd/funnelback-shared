package com.funnelback.plugin;

import com.funnelback.plugin.details.PluginDetails;
import com.funnelback.plugin.docs.PluginDocsDetails;
import com.funnelback.plugin.docs.model.ProductSubtopicCategory;

import java.util.List;

public interface PluginUtilsBase extends PluginDetails, PluginDocsDetails {
    @Override default List <ProductSubtopicCategory> getMetadataTags() {
        return getProductSubtopic();
    }
}
