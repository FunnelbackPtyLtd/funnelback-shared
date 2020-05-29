package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * An auto-completion display type which displays an HTML
 * fragment as the suggestion.
 *
 * Note that the supplier is responsible for ensuring
 * data sourced from any untrusted location has been
 * appropriately sanitized.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class HTMLFragment implements AutoCompletionDisplay {
    @NonNull private String html;
}
