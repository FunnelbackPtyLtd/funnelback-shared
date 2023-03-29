package com.funnelback.plugin.details;

import com.funnelback.plugin.details.model.PluginConfigFile;
import com.funnelback.plugin.details.model.PluginConfigKey;
import com.funnelback.plugin.details.model.PluginDetailsBase;

import java.util.List;

public interface PluginDetails extends PluginDetailsBase {
    String getPluginName();
    String getPluginDescription();
    List<PluginConfigKey> getConfigKeys();
    List<PluginConfigFile> getConfigFiles();
}
