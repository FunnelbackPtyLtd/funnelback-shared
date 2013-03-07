package com.funnelback.publicui.form.converter.operations;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Adds the live / preview banner tag
 */
public class AddPreviewBannerTag implements Operation {

    @Override
    public String process(String in) {
        return in.replaceAll("(?i)<body([^>]*)>", "<body$1>\n<@fb.ViewModeBanner />");
    }

}
