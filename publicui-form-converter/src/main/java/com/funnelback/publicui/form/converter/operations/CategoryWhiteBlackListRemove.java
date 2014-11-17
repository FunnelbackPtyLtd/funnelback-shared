package com.funnelback.publicui.form.converter.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Removes &lt;s:CategoryWhiteList&gt; and &lt;s:CategoryBlackList&gt; tags.
 */
@Slf4j
public class CategoryWhiteBlackListRemove implements Operation {

    @Override
    public String process(String in) {
        String out = in;
        
        Matcher m = Pattern.compile("<s:CategoryWhiteList>(.*?)</s:CategoryWhiteList>").matcher(out);
        if (m.find()) {
            List<String> terms = new ArrayList<String>();
            StringBuffer sb = new StringBuffer();
            do {
                m.appendReplacement(sb, "");
                terms.add(m.group(1));
            } while(m.find());
            m.appendTail(sb);
            out = sb.toString();
            log.warn("<s:CategoryWhiteList> tags have been removed. You'll need to add the following parameter "
                    + "to your collection.cfg to re-enable category white list:\n"
                    + "faceted_navigation.white_list.<Facet name>=" + StringUtils.join(terms, ","));
        }

        m = Pattern.compile("<s:CategoryBlackList>(.*?)</s:CategoryBlackList>").matcher(out);
        if (m.find()) {
            List<String> terms = new ArrayList<String>();
            StringBuffer sb = new StringBuffer();
            do {
                m.appendReplacement(sb, "");
                terms.add(m.group(1));
            } while(m.find());
            m.appendTail(sb);
            out = sb.toString();
            log.warn("<s:CategoryBlackList> tags have been removed. You'll need to add the following parameter "
                    + "to your collection.cfg to re-enable category black list:\n"
                    + "faceted_navigation.black_list.<Facet name>=" + StringUtils.join(terms, ","));
        }

        return out;
    }

}
