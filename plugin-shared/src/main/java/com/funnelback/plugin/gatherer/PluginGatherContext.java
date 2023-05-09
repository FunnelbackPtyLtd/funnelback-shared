package com.funnelback.plugin.gatherer;

import com.funnelback.plugin.PluginBaseConfigContext;
import com.funnelback.plugin.PluginBaseContext;

public interface PluginGatherContext extends PluginBaseContext, PluginBaseConfigContext {

    /**
     * Set the config settings for the current collection.
     *
     * @param key The name of the collection.cfg or global.cfg parameter
     * @param value The value to be set for the collection.cfg or global.cfg parameter
     */
    void setConfigSetting(String key, String value);
}
