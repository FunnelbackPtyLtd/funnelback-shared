package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.PluginConfigFile;
import com.funnelback.plugin.details.model.PluginConfigKeyDetails;
import com.funnelback.plugin.details.model.PluginTarget;
import com.funnelback.plugin.docs.model.Audience;
import com.funnelback.plugin.docs.model.MarketplaceSubtype;
import com.funnelback.plugin.docs.model.ProductSubtopicCategory;
import com.funnelback.plugin.docs.model.ProductTopic;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PluginUtilsStub implements PluginUtilsBase {
    private final String pluginId;
    private final String pluginName;
    private final String pluginDescription;
    private final List <PluginTarget> pluginTarget;
    private final List <PluginConfigKeyDetails> configKeys;
    private final List <PluginConfigFile> configFiles;
    private final List <Audience> audience;
    private final List <MarketplaceSubtype> marketplaceSubtype;
    private final List <ProductTopic> productTopic;
    private final List <ProductSubtopicCategory> productSubtopic;
    private final String filterClass;
    private final String jsoupFilterClass;
}
