package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:Select> ... </s:Select> tags
 */
@Slf4j
public class SelectConversion implements Operation {

    private final static Pattern SELECT_PATTERN = Pattern.compile("<s:Select>(.*?)</s:Select>", Pattern.MULTILINE | Pattern.DOTALL);
    private final static Pattern RANGE_PATTERN = Pattern.compile("<range>(.*?)</range>", Pattern.MULTILINE | Pattern.DOTALL);
    private final static Pattern NAME_PATTERN = Pattern.compile("<name>(.*?)</name>", Pattern.MULTILINE | Pattern.DOTALL);
    private final static Pattern OPT_PATTERN = Pattern.compile("<opt>(.*?)</opt>", Pattern.MULTILINE | Pattern.DOTALL);
    
    @Override
    public String process(String in) {
        
        
        Matcher m = SELECT_PATTERN.matcher(in);
        
        if (m.find()) {
            log.info("Processing <s:Select> tags");
            
            String out = "";
            int start = 0;
            do  {
                String replaced = "<@s.Select";
                final String select = m.group(1);

                Matcher mName = NAME_PATTERN.matcher(select);
                if (mName.find() && !"".equals(mName.group(1))) {
                    replaced += " name=\"" + mName.group(1)+ "\"";
                }

                Matcher mOpt = OPT_PATTERN.matcher(select);
                if (mOpt.find()) {
                    boolean first = true;
                    replaced += " options=[";
                    do {
                        if (first) {
                            first = false;
                        } else {
                            replaced += ", ";
                        }
                        replaced += "\"" + mOpt.group(1)+ "\"";
                        
                    } while (mOpt.find());
                    replaced += "]";
                }

                Matcher mRange = RANGE_PATTERN.matcher(select);
                if (mRange.find() && !"".equals(mRange.group(1))) {
                    replaced += " range=\"" + mRange.group(1)+ "\"";
                }

                replaced += " />";
                out += in.substring(start, m.start()) +  replaced;
                start = m.end();            
            } while    (m.find());
            // Append last part
            out += in.substring(start);
            
            return out;
        } else {
            return in;
        }
    
    }

}
