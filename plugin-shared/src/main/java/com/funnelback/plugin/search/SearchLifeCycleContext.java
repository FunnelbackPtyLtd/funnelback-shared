package com.funnelback.plugin.search;

import com.funnelback.plugin.search.model.SuggestionQuery;

import java.util.List;
import java.util.Optional;

/**
 * Context for search life cycle plugin
 */
public interface SearchLifeCycleContext {

    /**
     * Reads a configuration file for the currently running plugin.
     *
     * The configuration file read is for the currently running plugin and
     * either the data source or search package the plugin is currently
     * running on. When a plugin is run during a search, this will be the
     * results page (profile) the search is running on.
     *
     * Plugins can not read the configuration files for other plugins.
     *
     * @param filename the name of the configuration file to read.
     * @return empty if the file doesn't exist, otherwise the contents of the file.
     */
    public Optional<byte[]> pluginConfigurationFileAsBytes(String filename);

    /**
     * Reads a configuration file for the currently running plugin as a UTF-8 String.
     *
     * See: {@link #pluginConfigurationFileAsBytes(String)}
     *
     * @param filename the name of the configuration file to read.
     * @return empty if the file doesn't exist, otherwise the contents of the file.
     */
    public Optional<String> pluginConfigurationFile(String filename);

    /**
     * To query PADRE to get a query suggestion for query completion
     *
     * @param suggestionQueryBuilder
     * @return empty if suggestion doesn;t exist, otherwise the suggestion query returned by PADRE.
     */
    public List<String> getSuggestionQueryResult(SuggestionQuery suggestionQuery);

}
