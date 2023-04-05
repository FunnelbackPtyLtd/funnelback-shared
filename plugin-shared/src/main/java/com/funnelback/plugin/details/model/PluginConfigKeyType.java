package com.funnelback.plugin.details.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Define a type of plugin configuration key
 *
 * For example, if key value is expected to be `true` or `false`
 *<pre>{@code
 * new PluginConfigKeyType(PluginConfigKeyType.Format.BOOLEAN)
 * }</pre>
 *
 * or using a builder
 * <pre>{@code
 * PluginConfigKeyType.builder()
 *      .type(PluginConfigKeyType.Format.BOOLEAN)
 *      .build()
 * }</pre>
 *
 * If key value is expected to be a list of strings,
 *<pre>{@code
 * new PluginConfigKeyType(PluginConfigKeyType.Format.ARRAY, PluginConfigKeyType.Format.STRING)
 * }</pre>
 *
 * or using a builder
 * <pre>{@code
 * PluginConfigKeyType.builder()
 *      .type(PluginConfigKeyType.Format.ARRAY)
 *      .subtype(PluginConfigKeyType.Format.STRING)
 *      .build()
 * }</pre>
 */
@Getter
@Builder
public class PluginConfigKeyType {
    /**
     * Allowed types of plugin configuration key
     */
    @RequiredArgsConstructor
    public enum Format {
        ARRAY("array"),
        BOOLEAN("boolean"),
        DATE("date"),
        INTEGER("integer"),
        LONG("long"),
        METADATA("metadata"),
        PASSWORD("password"),
        STRING("string");

        private final String type;
    }

    /**
     * Type of plugin configuration key value
     */
    private final Format type;

    /**
     * If the type of plugin configuration key value is `FORMAT.ARRAY`,
     * define a type of item in an array
     */
    private final Format subtype;

    public PluginConfigKeyType(Format type) {
        this(type, null);
    }

    public PluginConfigKeyType(Format type, Format subtype) {
        this.type = type;
        if (type == Format.ARRAY) {
            if (subtype == null) {
                throw new IllegalArgumentException("Type 'ARRAY' requires to provide subtype but found null");
            }
            this.subtype = subtype;
        } else {
            this.subtype = null;
        }
    }
}
