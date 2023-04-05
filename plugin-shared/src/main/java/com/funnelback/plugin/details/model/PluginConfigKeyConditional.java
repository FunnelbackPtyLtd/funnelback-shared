package com.funnelback.plugin.details.model;

import lombok.*;

import java.util.List;

/**
 * Define plugin configuration keys' dependency on each other to display it
 * in plugin configuration admin UI and in auto-generated documentation.
 *
 * If the usage of key B depends on a specific value set for key A, this can define by
 * setting on a definition of key B `PluginConfigKeyConditional`, for example:
 *
 * Key B will be used only if key A has a value set to "X" or "Z
 * <pre>{@code
 * new PluginConfigKeyConditional<String>("plugin.keyA.config.example", List.of("X", "Z"))
 * }</pre>
 *
 * or using a builder
 * <pre>{@code
 * PluginConfigKeyConditional.<String>builder()
 *      .associatedKeyId("plugin.keyA.config.example")
 *      .associatedKeyValue("X")
 *      .associatedKeyValue("Z")
 *      .build()
 * }</pre>
 *
 * @param <T> type of key values
 */
@RequiredArgsConstructor
@Getter
@Builder
public class PluginConfigKeyConditional<T> {
    /**
     * Plugin configuration key
     */
    @NonNull private final String associatedKeyId;

    /**
     * List of values
     */
    @Singular @NonNull private final List<T> associatedKeyValues;
}
