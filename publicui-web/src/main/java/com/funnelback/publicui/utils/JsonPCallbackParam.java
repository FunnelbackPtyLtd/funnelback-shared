package com.funnelback.publicui.utils;

import com.google.common.base.CharMatcher;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * This limits what the Jsonp callback can be 
 * 
 * <p>The only way in which a JsonP callback can be exploited is if the original
 * page itself has a bug which allows the user to set the callback. What is done 
 * here helps prevent such their security flaw from being exploited with our
 * end-points. Further pen tests claim reflected JS and HTML as a flaw although
 * technically not because you need something else to interpret the response in
 * correctly or do the above, we block some suspicious requests rather than 
 * explain why they are wrong.</p>
 *
 */
@NoArgsConstructor
@Log4j2
public class JsonPCallbackParam {

    @Getter private String callback;
    
    private static final String VALID_CHARS = "$._-[]\"";
    
    public JsonPCallbackParam(String callback) {
        if(!isValid(callback)) {
            throw new IllegalArgumentException("Invalid callback function name: '" + callback + "'. Callback may only contains ASCII digits or "
                + " alphabetic characters or the chars in: " + VALID_CHARS);
        }
        this.callback = callback;
    }
    
    
    public static boolean isValid(String callback) {
        return CharMatcher.anyOf(VALID_CHARS)
                .or(CharMatcher.inRange('0', '9'))
                .or(CharMatcher.inRange('a', 'z'))
                .or(CharMatcher.inRange('A', 'Z'))
                .matchesAllOf(callback);
    }
    
    @Override
    public String toString() {
        return callback;
    }
}
