package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Convert Best Bets data tags (bbif, bbifnot bb)_...{})
 */
@Log
public class BestBetsConversion implements Operation {

	@Override
	public String process(String in) {
		
		String out = in;

		// Normalize field names
		out = out.replaceAll("final_bb_link", "bb_clickTrackingUrl");
		out = out.replaceAll("bb_desc", "bb_description");
		
		// Replace if tags
		Matcher m = Pattern.compile("bbif\\{bb_(\\w*)\\}\\{(.*)\\}", Pattern.MULTILINE).matcher(out);
		if (m.find()) {
			log.info("Processing bbif{} tags");
			out = m.replaceAll("<#if s.bb.$1?exists>$2</#if>");
		}

		m = Pattern.compile("bbifnot\\{bb_(\\w*)\\}\\{(.*)\\}", Pattern.MULTILINE).matcher(out);
		if (m.find()) {
			log.info("Processing bbifnot{} tags");
			out = m.replaceAll("<#if ! s.bb.$1?exists>$2</#if>");
		}
		
		// Replace data tags
		out = out.replaceAll("bb\\{bb_(\\w*)\\}", "\\${s.bb.$1}");

		return out;
	}

}
