package com.funnelback.publicui.form.converter.operations;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:Res> tags
 *
 */
@Log
public class ResConversion implements Operation {

	private static final String[][] MAPPING = {
		{"final_result_link", "clickTrackingUrl"},
		{"live_url", "liveUrl"},
		{"display_url", "displayUrl"},
		{"cache_url", "cacheUrl"},
		{"filetype", "fileType"},
		{"filesize", "fileSize"}
	};
	
	@Override
	public String process(String in) {
		String out = in;
		
		log.info("Processing <s:Res> tags");
		for (String[] mapping: MAPPING) {
			String previous = mapping[0];
			String actual = mapping[1];
			
			out = out.replaceAll("([>'\"])" + previous + "([<'\"])", "$1" + actual + "$2");
		}
		
		// Special case for metadata classes
		out = out.replaceAll("([>'\"])md_(\\w)([<'\"])", "$1metaData.$2$3");
		
		// Replace <s:Res> with FreeMarker model access tags ${}
		out = out.replaceAll("<s:Res>([^<]*)</s:Res>", "\\${s.result.$1}");
		
		if (out.contains("result.date")) {
			out = out.replaceAll("result\\.date", "result.date?date?string(\"d MMM yyyy\")");
			log.warn("The date formatting directive has been automatically added. This will cause errors if results have no date.");
		}
		
		return out;
		
	}

}
