package com.funnelback.plugin.search.mock;

import com.funnelback.mock.helpers.MapBackedPluginConfigurationFiles;
import com.funnelback.mock.helpers.PluginConfigurationFileSettingMock;
import com.funnelback.plugin.search.SearchLifeCycleContext;
import com.funnelback.plugin.search.model.SuggestionQuery;
import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockSearchLifeCycleContext implements SearchLifeCycleContext,
        PluginConfigurationFileSettingMock {
    @Delegate
    private final MapBackedPluginConfigurationFiles mapBackedPluginConfigurationFiles
            = new MapBackedPluginConfigurationFiles();

    private final Map<SuggestionQuery, List<String>> mockSuggestions = new HashMap<>();

    public void setMockSuggestions(SuggestionQuery suggestionQuery, List<String> expectedSuggestions) {
        mockSuggestions.put(suggestionQuery, expectedSuggestions);
    }

    public List<String> getSuggestionQueryResult(SuggestionQuery suggestionQuery) {
        return mockSuggestions.get(suggestionQuery);
    }
}
