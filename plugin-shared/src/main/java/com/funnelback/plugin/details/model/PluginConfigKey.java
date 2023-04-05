package com.funnelback.plugin.details.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Defines plugin configuration key properties
 *
 * For example, to define configuration key 'plugin.pluginID.config.foo.*'
 * <pre>{@code
 * new PluginConfigKey <String>(getPluginId(), "foo.*", "Key label", "Key desc", true, new PluginConfigKeyType(PluginConfigKeyType.Format.STRING), null, null, null)
 * }</pre>
 *
 * or using builder
 * <pre>{@code
 * PluginConfigKey.<String>builder()
 *      .pluginId(getPluginId())
 *      .id("foo.*")
 *      .label("Key label")
 *      .description("Key desc")
 *      .required(true)
 *      .type(new PluginConfigKeyType(PluginConfigKeyType.Format.STRING))
 * }</pre>
 *
 * - to define default value provide value in expected format  {@link #getDefaultValue()}
 * - to restrict allowed values {@link #getAllowedValue()}
 * - to set dependency of keys on each other {@link #getShowIfKeyHasValue()}
 *
 * @param <T> type of key value
 */
public class PluginConfigKey<T> extends PluginConfigKeyBase {
    /**
     * Define the default value of plugin configuration key
     */
    @Getter private final T defaultValue;

    /**
     * Define restriction on allowed plugin configuration key values,
     * for details see {@link PluginConfigKeyAllowedValue}
     */
    @Getter private final PluginConfigKeyAllowedValue<T> allowedValue;

    /**
     * Define conditional usage of plugin configuration key based on other key's value,
     * for details see {@link PluginConfigKeyConditional}
     */
    @Getter private final PluginConfigKeyConditional showIfKeyHasValue;

    @Builder
    public PluginConfigKey(@NonNull String pluginId, @NonNull String id, @NonNull String label, @NonNull String description, boolean required,
        @NonNull PluginConfigKeyType type, T defaultValue, PluginConfigKeyAllowedValue<T> allowedValue, PluginConfigKeyConditional showIfKeyHasValue) {
        super(pluginId, id, label, description, required, type);
        this.defaultValue = defaultValue;
        this.allowedValue = allowedValue;
        this.showIfKeyHasValue = showIfKeyHasValue;
    }
}
