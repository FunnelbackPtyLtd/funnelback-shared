package com.funnelback.plugin.details.model;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Define plugin configuration key allowed value
 *
 * To define fixed list of allowed values, set `values` {@link #values}.
 * For example, to allow only configuration key to be set to "A" or "Z":
 * <pre>{@code
 * new PluginConfigKeyAllowedValue(List.of("A", "Z"))
 * }</pre>
 *
 * To define set `regex` {@link #regex}.
 * For example, to allow value to be in format 'number.number' ie. 1.2, 3.4:
 * <pre>{@code
 * new PluginConfigKeyAllowedValue(Pattern.compile("\\d\\.\\d")
 * }</pre>
 *
 * @param <T> type of key value
 */
@Getter
public class PluginConfigKeyAllowedValue<T> {
    public enum AllowedType {
        FIXED_LIST, REGEX_LIST
    }

    /**
     * Type of allowed values
     */
    @NonNull private final AllowedType type;

    /**
     * Define a list of fixed values that plugin configuration key value can be set to
     * to display in plugin configuration admin UI and in auto-generated documentation
     */
    private final List<T> values;

    /**
     * Define regular expression to run against provided plugin configuration key value
     * to determine if a value is allowed to display in plugin configuration admin UI
     */
    private final Pattern regex;

    public PluginConfigKeyAllowedValue(@NonNull List <T> values) {
        this.regex = null;
        this.values = values;
        this.type = AllowedType.FIXED_LIST;
    }

    public PluginConfigKeyAllowedValue(@NonNull Pattern regex) {
        this.regex = regex;
        this.values = null;
        this.type = AllowedType.REGEX_LIST;
    }
}
