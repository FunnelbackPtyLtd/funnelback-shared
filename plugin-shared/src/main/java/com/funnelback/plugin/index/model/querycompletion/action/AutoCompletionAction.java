package com.funnelback.plugin.index.model.querycompletion.action;

/**
 * This interface groups the options for what action occurs when an auto completion suggestion is selected.
 *
 * Please note that plugins cannot provide new implementations of this interface.
 *
 * Funnelback supports the following types of actions occurring when an auto completion suggestion is selected
 * {@link ExtendQuery}
 * {@link JavaScriptCallback}
 * {@link OpenUrl}
 * {@link RunQuery}
 * {@link RunTriggerAsQuery}
 *
 * See the subclasses for further details on each option.
 **/
public interface AutoCompletionAction {
}
