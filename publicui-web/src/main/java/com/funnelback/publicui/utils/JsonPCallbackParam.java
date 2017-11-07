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
    
    public JsonPCallbackParam(String callback) {
        if(!isValid(callback)) {
            throw new IllegalArgumentException("Invalid callback function name: " + callback);
        }
        this.callback = callback;
    }
    
    
    public static boolean isValid(String callback) {
        // Must not be important HTML or java script chars.
        return !CharMatcher.anyOf("<>/\\()' \t,;{}").matchesAnyOf(callback)
            // MUst not be tricky control chars.
            && !CharMatcher.inRange((char) 0, (char) 31).matchesAnyOf(callback)
            // Must not have white space (although this does mean a callback of foo["a b"] is made invalid
            && !CharMatcher.whitespace().matchesAnyOf(callback);
    }
    
    @Override
    public String toString() {
        return callback;
    }
}
