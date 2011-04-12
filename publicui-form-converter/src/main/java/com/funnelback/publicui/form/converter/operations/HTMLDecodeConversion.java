package com.funnelback.publicui.form.converter.operations;

import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:htmldecode> tags. Assume that the content of
 * the tag has already been converted to a FreeMarker data access
 * tag ${...}
 */
@Log
public class HTMLDecodeConversion implements Operation {

	@Override
	public String process(String in) {
		
		String out = in;
		
		if (out.matches("(?is).*<s:htmldecode>.*")) {
			log.info("Processing <s:htmldecode> tags");
			out = out.replaceAll("(?i)<s:htmldecode>\\$\\{(.*?)\\}</s:htmldecode>", "\\$\\{htmlDecode($1)\\}");
		}

		return out;
	}

}
