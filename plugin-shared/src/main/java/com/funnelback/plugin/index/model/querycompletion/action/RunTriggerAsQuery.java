package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * An auto-completion action type which
 * replaces the user's current query with
 * the entry's trigger, causing a search results
 * page for the trigger to be shown.
 *
 * This is the default if no other action is
 * supplied.
 */
@AllArgsConstructor
@EqualsAndHashCode
public class RunTriggerAsQuery implements AutoCompletionAction {
}
