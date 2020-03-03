package com.funnelback.plugin.gatherer;

/**
 * A plugin may implement this if the plugin is to support gathering of documents.
 * 
 * The plugin will only be called on custom collections.
 *
 */
public interface PluginGatherer {

    /**
     * 
     * @param pluginGatherContext Provides access to collection config settings.
     * @param store Used by the plugin to store the documents into.
     * @throws Exception
     */
    public void gather(PluginGatherContext pluginGatherContext, PluginStore store) throws Exception;
}
