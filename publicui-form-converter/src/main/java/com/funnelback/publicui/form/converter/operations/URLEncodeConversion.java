package com.funnelback.publicui.form.converter.operations;

import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:URLEncode> tags. Assume that the content of
 * the tag has already been converted to a FreeMarker data access
 * tag ${...}
 */
@Log
public class URLEncodeConversion implements Operation {

	@Override
	public String process(String in) {
		
		String out = in;
		
		if (out.matches("(?is).*<s:URLEncode>.*")) {
			log.info("Processing <s:URLEncode> tags");
			out = out.replaceAll("(?i)<s:URLEncode>\\$\\{(.*?)\\}</s:URLEncode>", "\\$\\{$1?url\\}");
		}

		return out;
	}

}
