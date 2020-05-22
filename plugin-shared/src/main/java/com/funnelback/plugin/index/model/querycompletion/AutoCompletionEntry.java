package com.funnelback.plugin.index.model.querycompletion;

import com.funnelback.plugin.index.model.querycompletion.action.AutoCompletionAction;
import com.funnelback.plugin.index.model.querycompletion.display.AutoCompletionDisplay;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class AutoCompletionEntry {
    /**
     * Contains the text against which the partial query entered by the user is matched. Note that the
     * matching is performance case-insensitively.
     *
     * For example if this column contains "Australia", this suggestion will trigger if a user enters "aus".
     */
    private final String key;

    /**
     * Used to rank suggestions which matches the partial query, in case multiple suggestion matches a given partial query.
     * The expected range is 0-999.
     *
     * If the partial query is "do", both "dog" and "donut" will be returned. The one with the higher weight will be
     * returned first (assuming the auto-completion sort option is set to sort by score).
     */
    private final Double weight;

    /**
     * This is what will be displayed to the user for this suggestion.
     *
     * See the concrete classes in com.funnelback.plugin.index.model.querycompletion.display
     * for available options.
     */
    private AutoCompletionDisplay display = new com.funnelback.plugin.index.model.querycompletion.display.Default();

    /**
     * The category of the suggestion. This field allows to classify suggestion in multiple categories.
     *
     * If the suggestion concerns a product, you could use "Product" here. All the suggestions with the same category will
     * be displayed under the same category header in the search result page.
     */
    private String category = "";

    /**
     * Action that will be performed when the user selects this suggestion. It can be:
     *
     * See the concrete classes in com.funnelback.plugin.index.model.querycompletion.action
     * for available options.
     */
    private AutoCompletionAction action = new com.funnelback.plugin.index.model.querycompletion.action.Default();
}
