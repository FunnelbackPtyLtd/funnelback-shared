package com.funnelback.publicui.form.converter.operations;

import org.apache.commons.lang.StringUtils;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Insert header (FTL libraries, etc.)
 */
public class InsertHeader implements Operation {

    private static final String[] header = {
        "<#ftl encoding=\"utf-8\" />",
        "<#import \"/web/templates/modernui/funnelback_classic.ftl\" as s/>",
        "<#import \"/web/templates/modernui/funnelback.ftl\" as fb/>",
        ""
    };
    
    @Override
    public String process(String in) {
        return StringUtils.join(header, System.getProperty("line.separator"))
            + System.getProperty("line.separator")
            + in;
    }

}
