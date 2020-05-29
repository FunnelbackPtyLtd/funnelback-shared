package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * An auto-completion display type which provides
 * some plain text which will be displayed
 * as the auto-completion suggestion.
 *
 * The content provided will be HTML encoded
 * during presentation, preventing any
 * HTML tags being interpreted in this display.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class PlainText implements AutoCompletionDisplay {
    @NonNull private String text;
}
