package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Replaces IfDefCGI and IfNotDefCGI tags
 */
@Slf4j
public class IfDefCGIConversion implements Operation {
    
    @Override
    public String process(final String in) {

        String out = in;
        
        Matcher m = Pattern.compile("<s:IfDefCGI\\s+(.*?)>").matcher(out);
        if (m.find()) {
            log.info("Processing s:IfDefCGI tags");
            out = m.replaceAll("<@s.IfDefCGI name=\"$1\">");
            out = out.replaceAll("</s:IfDefCGI>", "</@s.IfDefCGI>");
        } else {
            log.info("No s:IfDefCGI tags processed");
        }

        m = Pattern.compile("<s:IfNotDefCGI\\s+(.*?)>").matcher(out);
        if (m.find()) {
            log.info("Processing s:IfNotDefCGI tags");
            out = m.replaceAll("<@s.IfNotDefCGI name=\"$1\">");
            out = out.replaceAll("</s:IfNotDefCGI>", "</@s.IfNotDefCGI>");
        } else {
            log.info("No s:IfNotDefCGI tags processed");
        }

        return out;
    }

}
