package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * An auto-completion action type which
 * replaces the user's current query with
 * the supplied one, causing a search results
 * page with the supplied query to be shown.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class RunQuery implements AutoCompletionAction {
    @NonNull private String queryToRun;
}
