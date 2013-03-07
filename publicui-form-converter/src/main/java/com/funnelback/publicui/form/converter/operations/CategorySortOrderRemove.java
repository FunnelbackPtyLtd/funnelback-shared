package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Removes <p>&lt;CategorySortOrder&gt;</p> tags.
 */
@Slf4j
public class CategorySortOrderRemove implements Operation {

    @Override
    public String process(String in) {
        String out = in;
        
        Matcher m = Pattern.compile("<s:CategorySortOrder.*?</s:CategorySortOrder>", Pattern.DOTALL).matcher(out);
        if (m.find()) {
            out = m.replaceAll("");
            log.warn("<s:CategorySortOrder> tags have been removed. You'll need to re-implement those with a"
                    + " faceted navigation transform script.");
        }
        
        
        return out;
    }

}
