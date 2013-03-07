package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts ResIf and ResIfNot tags
 */
@Slf4j
public class ResIfConversion implements Operation {

    @Override
    public String process(String in) {
        String out = in;
        
        Matcher m = Pattern.compile("<s:ResIf\\s+name=['\"]([^'\"]*)['\"]\\s*>").matcher(in);
        if (m.find()) {
            log.info("Processing <s:ResIf> tags");
            out = m.replaceAll("<#if s.result.$1?exists>");
            out = out.replaceAll("</s:ResIf>", "</#if>");
        }

        m = Pattern.compile("<s:ResIfNot\\s+name=['\"]([^'\"]*)['\"]\\s*>").matcher(out);
        if (m.find()) {
            log.info("Processing <s:ResIfNot> tags");
            out = m.replaceAll("<#if ! s.result.$1?exists>");
            out = out.replaceAll("</s:ResIfNot>", "</#if>");
        }
        
        m = Pattern.compile("<s:ResIfCollection\\s+name=['\"]([^\"']*)['\"]\\s*>").matcher(out);
        if (m.find()) {
            log.info("Processing <s:ResIfCollection> tags");
            out = m.replaceAll("<#if s.result.collection == \"$1\">");
            out = out.replaceAll("</s:ResIfCollection>", "</#if>");
        }

        m = Pattern.compile("<s:ResIfNotCollection\\s+name=['\"]([^\"']*)['\"]\\s*>").matcher(out);
        if (m.find()) {
            log.info("Processing <s:ResIfNotCollection> tags");
            out = m.replaceAll("<#if s.result.collection != \"$1\">");
            out = out.replaceAll("</s:ResIfNotCollection>", "</#if>");
        }

        return out;
    }

}
