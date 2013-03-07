package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Convert Best Bets data tags (bbif, bbifnot bb)_...{})
 */
@Slf4j
public class BestBetsConversion implements Operation {

    private static final String DEFAULT_TABLE_ATTR = "'bgcolor=\"#CCCCCC\" summary=\"Result header\" width=\"100%\" cellpadding=4 cellspacing=0";
    private static final String DEFAULT_HTML = "<p><table "+DEFAULT_TABLE_ATTR+"><tr><td><b>Best bets</b></td></tr></table></p>";

    @Override
    public String process(String in) {
        
        String out = in;

        // Normalize field names
        out = out.replaceAll("final_bb_link", "bb_clickTrackingUrl?html");
        out = out.replaceAll("bb_desc", "bb_description");
        
        // Replace if tags
        Matcher m = Pattern.compile("bbif\\{bb_(\\w*)\\}\\{(.*)\\}", Pattern.MULTILINE).matcher(out);
        if (m.find()) {
            log.info("Processing bbif{} tags");
            out = m.replaceAll("<#if s.bb.$1?exists>$2</#if>");
        }

        m = Pattern.compile("bbifnot\\{bb_(\\w*)\\}\\{(.*)\\}", Pattern.MULTILINE).matcher(out);
        if (m.find()) {
            log.info("Processing bbifnot{} tags");
            out = m.replaceAll("<#if ! s.bb.$1?exists>$2</#if>");
        }
        
        // Replace data tags
        out = out.replaceAll("bb\\{bb_([\\w\\?]*)\\}", "\\${s.bb.$1}");
        
        // Capture TierBar HTML fragment
        // Try to capture TierBar HTML fragments
        String htmlFragment = DEFAULT_HTML;
        m = Pattern.compile("<s:TierBarBestBets>(.*?)</s:TierBarBestBets>", Pattern.DOTALL).matcher(out);
        if (m.find()) {
            htmlFragment = m.group(1);    
            // Remove old tier bar tag
            out = m.replaceAll("");
        }
        // Insert HTML fragment after the <@s.BestBet> tag
        out = out.replace("<@s.BestBets>", "<@s.BestBets>" + htmlFragment);


        return out;
    }

}
