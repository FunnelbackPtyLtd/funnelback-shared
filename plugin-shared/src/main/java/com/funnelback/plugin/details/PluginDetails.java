package com.funnelback.plugin.details;

import com.funnelback.plugin.details.model.PluginConfigFile;
import com.funnelback.plugin.details.model.PluginConfigKeyDetails;
import com.funnelback.plugin.details.model.PluginDetailsBase;
import com.funnelback.plugin.details.model.PluginTarget;
import com.funnelback.plugin.docs.model.ProductSubtopicCategory;

import java.util.List;

public interface PluginDetails extends PluginDetailsBase {
    /**
     * Define human-readable plugin name to display in plugin configuration admin UI
     * and in auto-generated documentation
     */
    String getPluginName();

    /**
     * Define plugin short description or summary with purpose of usage to display in plugin configuration admin UI
     * and in auto-generated documentation
     */
    String getPluginDescription();

    /**
     * Define target for which plugin is executed
     * and source meta tag "plugin-scope" in auto-generated documentation
     */
    List<PluginTarget> getPluginTarget();

    /**
     * Define a list of metadata tags describing purpose of plugin
     * See {@link com.funnelback.plugin.docs.model.ProductSubtopic}
     */
    List<ProductSubtopicCategory> getMetadataTags();

    /**
     * Define a list of all plugin configuration keys to display in plugin configuration admin UI
     * and in auto-generated documentation
     */
    List<PluginConfigKeyDetails> getConfigKeys();

    /**
     * Define a list of all plugin configuration files to display in plugin configuration admin UI
     * and in auto-generated documentation
     */
    List<PluginConfigFile> getConfigFiles();

    /**
     * Define fully-qualified class name of filter to display in plugin configuration admin UI
     * and in auto-generated documentation
     */
    String getFilterClass();

    /**
     * Define fully-qualified class name of jsoup filter to display in plugin configuration admin UI
     * and in auto-generated documentation
     */
    String getJsoupFilterClass();
}
