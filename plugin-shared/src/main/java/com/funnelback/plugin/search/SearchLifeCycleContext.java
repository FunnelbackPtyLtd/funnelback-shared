package com.funnelback.plugin.search;

import com.funnelback.plugin.PluginBaseConfigFileContext;
import com.funnelback.plugin.search.model.SuggestionQuery;

import java.util.List;

/**
 * Context for search life cycle plugin
 */
public interface SearchLifeCycleContext extends PluginBaseConfigFileContext {

    /**
     * To query PADRE to get a query suggestion for query completion
     *
     * @param suggestionQuery query posed for search
     * @return empty if suggestion doesn;t exist, otherwise the suggestion query returned by PADRE.
     */
    List<String> getSuggestionQueryResult(SuggestionQuery suggestionQuery);

}
