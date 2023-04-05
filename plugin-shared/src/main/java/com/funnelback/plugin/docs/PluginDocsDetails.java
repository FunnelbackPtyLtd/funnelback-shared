package com.funnelback.plugin.docs;

import com.funnelback.plugin.docs.model.*;

import java.util.List;

public interface PluginDocsDetails {
    /**
     * Define a list of tags to display in documentation as meta tag "hc-audience"
     */
    List<Audience> getAudience();

    /**
     * Define a list of tags to display in documentation as meta tag "marketplace-subtype"
     */
    List<MarketplaceSubtype> getMarketplaceSubtype();

    /**
     * Define a list of tags to display in documentation as meta tag "product-topic"
     */
    List<ProductTopic> getProductTopic();

    /**
     * Define a list of tags to display in documentation as meta tag "product-subtopic"
     * and source metadata tags about plugin
     */
    List<ProductSubtopicCategory> getProductSubtopic();
}
