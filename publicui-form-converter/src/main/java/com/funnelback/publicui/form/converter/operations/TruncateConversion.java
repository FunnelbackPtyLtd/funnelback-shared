package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts s:Truncate & s:TruncateURL tags
 */
@Slf4j
public class TruncateConversion implements Operation {

    @Override
    public String process(String in) {
        String out = in;
        
        Matcher m = Pattern.compile("<s:Truncate\\s+(\\d+)([^>]*)>").matcher(out);
        if (m.find()) {
            log.info("Processing <s:Truncate tags");
            out = m.replaceAll("<@s.Truncate length=$1$2>");
            out = out.replaceAll("</s:Truncate>", "</@s.Truncate>");
            
            // Replace 'stripMiddle' option by a boolean
            out = out.replaceAll("stripMiddle", "stripMiddle=true");
        }
        
        m = Pattern.compile("<s:TruncateURL\\s+(\\d+)([^>]*)>").matcher(out);
        if (m.find()) {
            log.info("Processing <s:TruncateURL tags");
            out = m.replaceAll("<@s.TruncateURL length=$1$2>");
            out = out.replaceAll("</s:TruncateURL>", "</@s.TruncateURL>");
        }

        
        return out;
    }

}
