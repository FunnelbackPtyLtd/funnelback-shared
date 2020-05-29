package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * An auto-completion action type which adds
 * the given suffix to the user's current
 * query and then runs the combined query.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ExtendQuery implements AutoCompletionAction {
    @NonNull private String querySuffixToAdd;
}
