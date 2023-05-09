package com.funnelback.common.filter.jsoup;

import com.funnelback.plugin.PluginBaseConfigContext;
import com.funnelback.plugin.PluginBaseContext;

import java.io.File;

/**
 * Represents the 'setup' of a filter, which includes information
 * about the Funnelback installation and collection for which
 * documents will be filtered.
 */
public interface SetupContext extends PluginBaseContext, PluginBaseConfigContext {

    /**
     * Set the config settings for the current collection.
     *
     * @param key The name of the collection.cfg or global.cfg parameter
     * @param value The value to be set for the collection.cfg or global.cfg parameter
     */
    void setConfigSetting(String key, String value);

    /**
     * <p>Provides access to configuration files inside the collection configuration folder.</p>
     * 
     * <p>This is useful to access custom collection-level configuration files</p>
     *  
     * @param filename Name of the file to access
     * @return A {@link File} path pointing to the desired file inside the collection configuration folder
     */
    File getCollectionConfigFile(String filename);
}
