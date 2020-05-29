package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.net.URI;

/**
 * An auto-completion action type which
 * directly opens the given URL rather than
 * changing the search page.
 *
 * This is useful when the user is selecting
 * a completion which itself is representative
 * of a result (e.g. a person's name in a
 * staff-directory context).
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class OpenUrl implements AutoCompletionAction {
    @NonNull private URI urlToOpen;
}
