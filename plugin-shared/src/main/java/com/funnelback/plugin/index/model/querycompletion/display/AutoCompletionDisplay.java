package com.funnelback.plugin.index.model.querycompletion.display;

/**
 * This interface groups the options for how auto completion suggestions are displayed.
 *
 * Please note that plugins cannot provide new implementations of this interface.
 *
 * Funnelback supports the following types of content being displayed in auto completion suggestions
 * {@link DisplayTrigger}
 * {@link HTMLFragment}
 * {@link JavaScriptCallbackDisplay}
 * {@link JSONData}
 * {@link PlainText}
 *
 * See the subclasses for further details on each option.
 **/
public interface AutoCompletionDisplay {
}
