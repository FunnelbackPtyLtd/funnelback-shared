package com.funnelback.publicui.utils;

import com.google.common.base.CharMatcher;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor
@Log4j2
public class JsonPCallbackParam {

    @Getter private String callback;
    
    public JsonPCallbackParam(String callback) {
        log.fatal("Called with: " + callback);
        if(!isValid(callback)) {
            throw new IllegalArgumentException("Invalid callback function name: " + callback);
        }
        this.callback = callback;
    }
    
    
    public static boolean isValid(String callback) {
        return !CharMatcher.anyOf("<>/\\\"'()").matchesAnyOf(callback);
    }
    
    @Override
    public String toString() {
        return callback;
    }
}
