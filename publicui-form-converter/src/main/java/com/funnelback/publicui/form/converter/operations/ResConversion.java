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
		{"final_result_link", "clickTrackingUrl?html"},
		{"live_url", "liveUrl?html"},
		{"display_url", "displayUrl?html"},
		{"cache_url", "cacheUrl?html"},
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
		out = out.replaceAll("([>'\"])md_(\\w)([<'\"])", "$1metaData.$2!$3");
		
		// Replace <s:Res> with FreeMarker model access tags ${}
		out = out.replaceAll("<s:Res>([^<]*)</s:Res>", "\\${s.result.$1}");
		
		// Place filesize formatting method
		out = out.replace("${s.result.fileSize}", "${filesize(s.result.fileSize)}");
		
		if (out.contains("result.date")) {
			out = out.replaceAll("result\\.date", "result.date?date?string(\"d MMM yyyy\")");
			log.warn("<s:Res>date</s:Res> tags have been converted to ${result.date?date?string(\"d MMM yyy\")}. "
					+ "This will cause errors if results have no date, so you should manually check these statements"
					+ " and possibly test for the presence of a date before trying to format it."
					);
		}
		
		return out;
		
	}

}
