package com.funnelback.mock.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MapBackedPluginConfigurationFiles {
    
    private final Map<String, byte[]> filesContents = new HashMap<>();
    
    public void setPlugingConfigurationFileContent(String filename, String content) {
        filesContents.put(filename, content.getBytes());
    }
    
    public void setPlugingConfigurationFileContentAsBytes(String filename, byte[] content) {
        filesContents.put(filename, content);
    }
    
    public Optional<byte[]> pluginConfigurationFileAsBytes(String filename) {
        return Optional.ofNullable(filesContents.get(filename));
    }

    public Optional<String> pluginConfigurationFile(String filename) {
        return pluginConfigurationFileAsBytes(filename).map(b -> new String(b, UTF_8));
    }
}
