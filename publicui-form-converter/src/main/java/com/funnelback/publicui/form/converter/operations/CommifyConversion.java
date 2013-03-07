package com.funnelback.publicui.form.converter.operations;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:commify> tags.
 * 
 * Assumes that previous conversions already took place.
 * Expects the tags to be in a semi-converted form already accessing
 * the FreeMarker data model:
 * 
 * <s:commify>${response...}</s:commify>
 *
 */
@Slf4j
public class CommifyConversion implements Operation {

    @Override
    public String process(String in) {
        String out = in;
        
        if (out.matches("(?is).*<s:commify>.*")) {
            log.info("Processing <s:commify> tags");
            out = out.replaceAll("(?i)<s:commify>[^\\$]*\\$\\{([^\\}]*)\\}[^<]*</s:commify>", "\\${$1?string.number}");
        }
        
        return out;
    }

}
