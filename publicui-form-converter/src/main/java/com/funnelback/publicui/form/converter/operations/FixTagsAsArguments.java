package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Try to fix tags used as arguments, such as:
 * &lt;s:boldicize bold="&lt;@s.cgi&gt;query&lt;/@s.cgi&gt;">
 *
 */
public class FixTagsAsArguments implements Operation {

    private static final String TAG_PATTERN_START = "<(\\S+\\s+\\S+=)['\"]";
    private static final String TAG_PATTERN_END = "['\"](\\s*)>";
    
    @Override
    public String process(String in) {
        String out = in;
        
        Matcher m = Pattern.compile(TAG_PATTERN_START + "<@s\\.cgi>(.*?)</@s\\.cgi>" + TAG_PATTERN_END, Pattern.CASE_INSENSITIVE).matcher(out);
        if (m.find()) {
            out = m.replaceAll("<$1 ${question.inputParameterMap[\"$2\"]} $2>");
        }
        
        return out;
    }

}
