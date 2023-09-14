package com.funnelback.plugin.details.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.funnelback.common.utils.SharedConfigKeyUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@AllArgsConstructor
public abstract class PluginConfigKeyBase implements PluginConfigKeyDetails {
    /**
     * ID of a current plugin
     */
    @Getter @NonNull @JsonIgnore private final String pluginId;

    /**
     * ID of key to create plugin configuration key
     *
     * For example, to create
     * - key `plugin.pluginID.config.foo` provide `id="foo"`
     * - key `plugin.pluginID.config.foo.bar` provide `id="foo.bar"`
     * - key with wildcard `plugin.pluginID.config.foo.*` provide `id="foo.*"`
     * - key with wildcard `plugin.pluginID.config.foo.*.baz` provide `id="foo.*.baz"`
     * - key with wildcard `plugin.pluginID.config.*.*.foo` provide `id="*.*.foo"`
     */
    @NonNull @JsonProperty( access = JsonProperty.Access.READ_WRITE) protected final String id;

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

    /**
     * Create plugin key based on provided key ID
     *
     * If ID was defined with wildcard, this method will return key in wildcard form.
     * To resolve wildcard to particular value see {@link #getKey(String...)}
     *
     * For example:
     * - for ID `foo`, `getKey()` -> `plugin.pluginID.config.foo`,
     * - for ID `foo.bar`, `getKey()` -> `plugin.pluginID.config.foo.bar`
     * - for ID `*.*.foo`, `getKey()` -> `plugin.pluginID.config.*.*.foo`
     *
     * @return plugin configuration key
     */
    public String getKey() {
        return getKeyPrefix() + id;
    }

    /**
     * Create plugin key based on provided key ID with resolving wildcards to provided values
     *
     * For example:
     * - for ID `foo.*`, `getKey("something")` -> `plugin.pluginID.config.foo.something`
     * - for ID `*.*.foo`, `getKey("something", "more")` -> `plugin.pluginID.config.something.more.foo`
     *
     * @param wildcard list of wildcard values to replace in key
     * @return plugin configuration key with resolved wildcard value
     */
    public String getKey(String ...wildcard) {
        return SharedConfigKeyUtils.getKeyWithWildcard(getKey(), Arrays.asList(wildcard));
    }

}
