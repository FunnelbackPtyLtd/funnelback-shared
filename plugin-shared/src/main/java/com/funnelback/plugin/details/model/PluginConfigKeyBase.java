package com.funnelback.plugin.details.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.funnelback.common.utils.SharedConfigKeyUtils;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;

public abstract class PluginConfigKeyBase implements PluginConfigKeyDetails {
    /**
     * ID of a current plugin
     */
    @Getter @NonNull @JsonIgnore private final String pluginId;

    /**
     * ID of a key to create a plugin configuration key
     *
     * For example, to create
     * - key `plugin.pluginID.config.foo` provide `id="foo"`
     * - key `plugin.pluginID.config.foo.bar` provide `id="foo.bar"`
     * - key with wildcard `plugin.pluginID.config.foo.*` provide `id="foo.*"`
     * - key with wildcard `plugin.pluginID.config.foo.*.baz` provide `id="foo.*.baz"`
     * - key with wildcard `plugin.pluginID.config.foo.*.baz.*` provide `id="foo.*.baz.*"`
     */
    @NonNull @JsonProperty(access = JsonProperty.Access.READ_WRITE) protected final String id;

    /**
     * Label for plugin configuration key to display in plugin configuration admin UI
     */
    @Getter @NonNull private final String label;

    /**
     * Short description of plugin configuration key to display in plugin configuration admin UI
     * and in auto-generated documentation
     */
    @Getter @NonNull private final String description;

    /**
     * Longer description of plugin configuration key to display in plugin auto-generated documentation
     */
    @Getter private String longDescription;

    /**
     * Mark plugin configuration key as required or optional in plugin configuration admin UI
     * and in auto-generated documentation
     */
    @Getter private final boolean required;

    /**
     * Type of plugin configuration key,
     * for details see {@link PluginConfigKeyType}
     */
    @Getter @NonNull private final PluginConfigKeyType type;

    public PluginConfigKeyBase(@NonNull String pluginId, @NonNull String id, @NonNull String label, @NonNull String description, boolean required, @NonNull PluginConfigKeyType type) {
        this(pluginId, id, label, description, null, required, type);
    }

    public PluginConfigKeyBase(@NonNull String pluginId, @NonNull String id, @NonNull String label, @NonNull String description, String longDescription, boolean required, @NonNull PluginConfigKeyType type) {
        SharedConfigKeyUtils.validateKey(id);
        this.pluginId = pluginId;
        this.id = id;
        this.label = label;
        this.description = description;
        this.longDescription = longDescription;
        this.required = required;
        this.type = type;
    }

    /**
     * Create a plugin key based on provided key ID
     *
     * If ID was defined with wildcard, this method will return key in wildcard form.
     * To resolve wildcard to particular value see {@link #getKey(String...)}
     *
     * For example:
     * - for ID `foo`, `getKey()` returns `plugin.pluginID.config.foo`,
     * - for ID `foo.bar`, `getKey()` returns `plugin.pluginID.config.foo.bar`
     * - for ID `foo.*`, `getKey()` returns `plugin.pluginID.config.foo.*`
     *
     * @return plugin configuration key
     */
    public String getKey() {
        return getKeyPrefix() + id;
    }

    /**
     * Create a plugin key based on provided key ID with resolving wildcards to provided values
     *
     * For example:
     * - for ID `foo.*`, `getKey("something")` returns `plugin.pluginID.config.foo.something`
     * - for ID `*.foo.*.bar`, `getKey("something", "more")` returns `plugin.pluginID.config.something.foo.more.bar`
     *
     * @param wildcard list of wildcard values to replace in key
     * @return plugin configuration key with resolved wildcard value
     */
    public String getKey(String ...wildcard) {
        return SharedConfigKeyUtils.getKeyWithWildcard(getKey(), Arrays.asList(wildcard));
    }
}
