package com.funnelback.plugin.index.model.querycompletion;

import com.funnelback.plugin.index.model.querycompletion.action.AutoCompletionAction;
import com.funnelback.plugin.index.model.querycompletion.action.RunTriggerAsQuery;
import com.funnelback.plugin.index.model.querycompletion.display.AutoCompletionDisplay;
import com.funnelback.plugin.index.model.querycompletion.display.DisplayTrigger;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents an entry to be provided as additional data for auto-completion.
 *
 * In the simplest case, just a trigger can be provided, which causes the trigger
 * string to be available as a possible auto-completion which, if selected, will submit
 * the trigger string itself as a new search query.
 *
 * To include the string 'funnelback' as such a suggestion, the entry would be
 * constructed as follows.
 *
 * <pre>{@code
 * AutoCompletionEntry.builder().trigger("funnelback").build()
 * }</pre>
 *
 * A more complete example which uses all available fields is as follows...
 *
 * <pre>{@code
 * AutoCompletionEntry.builder()
 *     .trigger("funnelback")
 *     .weight(3.14)
 *     .display(new PlainText("Would you like to append 'funnelback enterprise search' to your query?"))
 *     .category("query-extensions")
 *     .action(new ExtendQuery("funnelback enterprise search"))
 *     .build()
 * }</pre>
 *
 * This example triggers when the user enters a partial query which matches 'funnelback'
 * (with a slightly higher than default priority), displays the text
 * 'Would you like to append 'funnelback enterprise search' to your query?' within a
 * query-extensions category and, if selected, would append 'funnelback enterprise search'
 * to the user's current query and run it.
 *
 * See the {@link com.funnelback.plugin.index.model.querycompletion.display} and
 * {@link com.funnelback.plugin.index.model.querycompletion.action} packages respectively
 * for other types of displays and actions available.
 *
 */
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class AutoCompletionEntry {
    /**
     * Contains the text against which the partial query entered by the user is matched. Note that the
     * matching is performance case-insensitively.
     *
     * For example if this column contains "Australia", this suggestion will trigger if a user enters "aus".
     */
    @NonNull
    private final String trigger;

    /**
     * Used to rank suggestions which matches the partial query, in case multiple suggestion matches a given partial query.
     * The expected range is 0-999.
     *
     * If the partial query is "do", both "dog" and "donut" will be returned. The one with the higher weight will be
     * returned first (assuming the auto-completion sort option is set to sort by score).
     */
    @NonNull
    @Builder.Default
    private final Double weight = 1.0;

    /**
     * This is what will be displayed to the user for this suggestion.
     *
     * See the concrete classes in com.funnelback.plugin.index.model.querycompletion.display
     * for available options.
     */
    @NonNull
    @Builder.Default
    private AutoCompletionDisplay display = new DisplayTrigger();

    /**
     * The category of the suggestion. This field allows to classify suggestion in multiple categories.
     *
     * If the suggestion concerns a product, you could use "Product" here. All the suggestions with the same category will
     * be displayed under the same category header in the search result page.
     */
    @NonNull
    @Builder.Default
    private String category = "";

    /**
     * Action that will be performed when the user selects this suggestion. It can be:
     *
     * See the concrete classes in com.funnelback.plugin.index.model.querycompletion.action
     * for available options.
     */
    @NonNull
    @Builder.Default
    private AutoCompletionAction action = new RunTriggerAsQuery();
}
