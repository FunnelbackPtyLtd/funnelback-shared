package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts &gt;s:cgi&lt; tags
 */
@Log
public class CGIConversion implements Operation {

	private static final Pattern PATTERN = Pattern.compile("<s:cgi>(.*?)</s:cgi>", Pattern.CASE_INSENSITIVE);
	
	@Override
	public String process(String in) {
		String out = in;
		
		Matcher m = PATTERN.matcher(out);
		if (m.find()) {
			log.info("Processing <s:cgi> tags");
			out = m.replaceAll("\\${question.inputParameterMap[\"$1\"]!?first!?html}");
		}
		
		return out;
	}

}
