package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * An auto-completion display type which provides
 * JSON data which can be templated within the
 * auto-completion's javascript code as required.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JSONData implements AutoCompletionDisplay {
    @NonNull private String json;
}
