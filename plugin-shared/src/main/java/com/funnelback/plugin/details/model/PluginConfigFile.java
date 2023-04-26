package com.funnelback.plugin.details.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Defines plugin configuration file properties
 *
 * For example, to define JSON file
 * <pre>{@code
 * new PluginConfigFile("rules.json", "Plugin rules", "Define a list of rules to extract data", "json", true)
 * }</pre>
 *
 * or using a builder
 * <pre>{@code
 * PluginConfigFile.builder()
 *      .name("rules.json")
 *      .label("Plugin rules")
 *      .description("Define a list of rules to extract data")
 *      .format("json")
 *      .required(true)
 *      .build()
 * }</pre>
 */
@RequiredArgsConstructor
@Getter
@Builder
public class PluginConfigFile {
    /**
     * Plugin configuration file name
     * For example: rules.json, properties.cfg
     */
    @NonNull private final String name;

    /**
     * Label for plugin configuration file to display in plugin configuration admin UI
     */
    @NonNull private final String label;

    /**
     * Description of plugin configuration file to display in plugin configuration admin UI
     * and in auto-generated documentation
     */
    @NonNull private final String description;

    /**
     * Expected format of plugin configuration file
     * For example: json, xml, txt
     */
    @NonNull private final String format;

    /**
     * Mark plugin configuration file as required or optional in plugin configuration admin UI
     * and in auto-generated documentation
     */
    private final boolean required;
}
