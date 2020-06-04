package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * An auto-completion display type which invokes a
 * JavaScript callback which is expected to return
 * HTML, which is then used as the display.
 *
 * Note that the HTML is not sanitised by Funnelback
 * so this should only be used for trusted sources.
 *
 * An example value might be `fetchStockPrice('some-suggestion-specific-code')`
 * in the context of a results page which defines the following function...
 *
 * <pre>{@code
 * function fetchStockPrice(symbol) {
 *    // Fetch stock market data
 *    var data = remoteServiceCall(symbol);
 *    ...
 *
 *    return data.companyName + ' (' + symbol + ') $' + data.currentPrice + ' (Higher: ' + data.hiPrice + ')';
 * }
 * }</pre>

 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JavaScriptCallback implements AutoCompletionDisplay {
    @NonNull private String code;
}
