package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:italicize> tags.
 * 
 * Assumes that some conversions already took place.
 * Expects the tag to be in a semi-converted form accessing
 * the FreeMarker data model:
 * 
 * <s:italicize italics="${SearchTransaction...}">${...}</s:italicize>
 */
@Slf4j
public class ItalicizeConversion implements Operation {

	@Override
	public String process(String in) {
		String out = in;
		
		Matcher m = Pattern.compile("<s:italicize\\s+italics=['\"]\\$\\{([^\\}]*)\\}['\"]\\s*>", Pattern.CASE_INSENSITIVE).matcher(out);
		if (m.find()) {
			log.info("Processing <s:italicize> tags");
			out = m.replaceAll("<@s.italicize italics=$1>");
			
			out = out.replaceAll("(?i)</s:italicize>", "</@s.italicize>");
		}
		
		return out;
	}

}
