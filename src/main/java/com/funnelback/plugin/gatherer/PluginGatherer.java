package com.funnelback.plugin.gatherer;

public interface PluginGatherer {

    /**
     * 
     * @param pluginGatherContext Provides access to collection config settings.
     * @param store Used by the plugin to store the documents into.
     * @throws Exception
     */
    public void gather(PluginGatherContext pluginGatherContext, PluginStore store) throws Exception;
}
