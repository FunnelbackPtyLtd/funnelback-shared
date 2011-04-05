package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts ResIf and ResIfNot tags
 */
@Log
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

		m = Pattern.compile("<s:ResIfNot\\s+name=['\"]([^'\"]*)['\"]\\s*>").matcher(in);
		if (m.find()) {
			log.info("Processing <s:ResIfNot> tags");
			out = m.replaceAll("<#if ! s.result.$1?exists>");
			out = out.replaceAll("</s:ResIfNot>", "</#if>");
		}

		return out;
	}

}
