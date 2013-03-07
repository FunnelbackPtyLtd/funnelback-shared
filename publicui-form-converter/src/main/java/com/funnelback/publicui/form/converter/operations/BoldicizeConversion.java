package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:boldicize> tags.
 * 
 * Assumes that some conversions already took place.
 * Expects the tag to be in a semi-converted form accessing
 * the FreeMarker data model:
 * 
 * <s:boldicize bold="${SearchTransaction...}">${...}</s:boldicize>
 */
@Slf4j
public class BoldicizeConversion implements Operation {

    @Override
    public String process(String in) {
        String out = in;
        
        Matcher m = Pattern.compile("<s:boldicize\\s+bold=['\"]\\$\\{([^\\}]*)\\}['\"]\\s*>", Pattern.CASE_INSENSITIVE).matcher(out);
        if (m.find()) {
            log.info("Processing <s:boldicize> tags");
            out = m.replaceAll("<@s.boldicize bold=$1>");
            
            out = out.replaceAll("(?i)</s:boldicize>", "</@s.boldicize>");
        }
        
        return out;
    }

}
