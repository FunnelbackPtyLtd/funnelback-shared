package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * An auto-completion display type which simply displays the trigger
 * string as the auto-completion suggestion.
 *
 * This is the default if no other display is specified.
 */
@AllArgsConstructor
@EqualsAndHashCode
public class DisplayTrigger implements AutoCompletionDisplay {
}
