package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * An auto-completion action type which executes
 * the given JavaScript code when the completion
 * is selected. The code itself is expected to
 * have some side-effect which modifies the
 * page or navigates in response to the user's
 * action.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JavascriptCallback implements AutoCompletionAction {
    @NonNull private String callbackCode;
}
