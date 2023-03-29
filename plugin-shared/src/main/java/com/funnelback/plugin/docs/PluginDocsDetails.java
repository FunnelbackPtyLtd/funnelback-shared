package com.funnelback.plugin.docs;

import com.funnelback.plugin.docs.model.*;

import java.util.List;

public interface PluginDocsDetails {
    List<Audience> getAudience();
    List<MarketplaceSubtype> getMarketplaceSubtype();
    List<ProductTopic> getProductTopic();
    List<ProductSubtopicCategory> getProductSubtopic();
}
