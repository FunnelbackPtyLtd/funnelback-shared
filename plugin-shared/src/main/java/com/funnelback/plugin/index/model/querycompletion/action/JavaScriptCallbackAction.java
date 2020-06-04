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
 *
 * An example value might be `refreshData('some-suggestion-specific-code')`
 * in the context of a results page which defines the following function...
 *
 * <pre>{@code
 *  function refreshData(symbol) {
 *    // Fetch stock market data
 *    var data = remoteServiceCall(symbol);
 *    ...
 *
 *    document.getElementById('price_info').value = data.currentPrice;
 *    if (data.currentPrice > data.lastWeekPrice) {
 *       document.getElementById('status').src = 'green.png';
 *    }
 * }
 * }</pre>
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JavaScriptCallbackAction implements AutoCompletionAction {
    @NonNull private String callbackCode;
}
