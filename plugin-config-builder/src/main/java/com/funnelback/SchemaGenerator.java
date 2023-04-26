package com.funnelback;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.PluginDetails;

import java.io.File;
import java.io.IOException;

public class SchemaGenerator {
    final static String FILE_NAME = "plugin-schema.json";
    private final PluginUtilsBase pluginUtils;
    private final String resourcesPath;
    private final ObjectMapper objectMapper;

    SchemaGenerator(PluginUtilsBase pluginUtils, String resourcesPath) {
        this.pluginUtils = pluginUtils;
        this.resourcesPath = resourcesPath;

        objectMapper = JsonMapper.builder()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();
    }

    void generate() throws IOException {
        objectMapper
            .writerFor(PluginDetails.class)
            .writeValue(new File(resourcesPath, FILE_NAME), pluginUtils);
    }
}
