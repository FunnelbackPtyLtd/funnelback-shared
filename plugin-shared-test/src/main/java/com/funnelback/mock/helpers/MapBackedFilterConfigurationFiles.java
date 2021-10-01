package com.funnelback.mock.helpers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MapBackedFilterConfigurationFiles {
    
    private final Map<String, byte[]> filesContents = new HashMap<>();
    
    public void setFilterConfigurationFileContent(String filename, String content) {
        filesContents.put(filename, content.getBytes());
    }
    
    public void setFilterConfigurationFileContentAsBytes(String filename, byte[] content) {
        filesContents.put(filename, content);
    }
    
    public Optional<byte[]> filterConfigurationFileAsBytes(String filename) {
        return Optional.ofNullable(filesContents.get(filename));
    }

    public Optional<String> filterConfigurationFile(String filename) {
        return filterConfigurationFileAsBytes(filename).map(b -> new String(b, UTF_8));
    }

    public File getCollectionConfigFile(String filename) {
        throw new RuntimeException("Not mocked.");
    }
}
