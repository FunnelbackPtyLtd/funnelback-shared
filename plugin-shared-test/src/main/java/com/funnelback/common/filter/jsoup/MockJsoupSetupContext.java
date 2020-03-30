package com.funnelback.common.filter.jsoup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

public class MockJsoupSetupContext implements SetupContext {

    @Getter @Setter private File searchHome;

    @Getter @Setter private String collectionName;
    
    /**
     * Set in this the config options to use.
     */
    @Getter private Map<String, String> configOptions = new HashMap<>();
    
    @Override
    public String getConfigSetting(String key) {
        return configOptions.get(key);
    }

    @Override
    public Set<String> getConfigKeysWithPrefix(String prefix) {
        return configOptions.keySet().stream().filter((a) -> a.startsWith(prefix)).collect(Collectors.toSet());
    }

    @Override
    public Map<String, List<String>> getConfigKeysMatchingPattern(String pattern) {
        throw new RuntimeException("Not yet mocked, this will be done in the future.");
    }

    @Override
    public File getCollectionConfigFile(String filename) {
        throw new RuntimeException("Not mocked.");
    }

}
