package com.funnelback.plugin.details.model;

import lombok.Builder;
import lombok.NonNull;

/**
 * Defines plugin configuration key  properties for storing secret information (like passwords)
 *
 * For example, to define configuration key 'plugin.pluginID.encrypted.token'
 * <pre>{@code}
 * new PluginConfigKeyEncrypted("pluginID",  "token", "Key label", "Key desc",  true);
 * </pre>
 *
 * or using a builder
 * <pre>{@code
 * PluginConfigKeyEncrypted.builder()
 *      .pluginId("pluginID")
 *      .id("token")
 *      .label("Key label")
 *      .description("Key desc")
 *      .required(true)
 *      .build();
 * }</pre>
 */
public class PluginConfigKeyEncrypted extends PluginConfigKeyBase {

    @Builder
    public PluginConfigKeyEncrypted(@NonNull String pluginId, @NonNull String id, @NonNull String label,
        @NonNull String description, boolean required) {
        super(pluginId, id, label, description, required, new PluginConfigKeyType(PluginConfigKeyType.Format.PASSWORD));
    }

    public String getKeyPrefix() {
        return PLUGIN_PREFIX + getPluginId() + PLUGIN_ENCRYPTED_QUALIFIER;
    }

}
