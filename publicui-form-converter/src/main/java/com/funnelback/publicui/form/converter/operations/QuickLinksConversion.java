package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts Quick Links data tags
 */
@Log
public class QuickLinksConversion implements Operation {
	
	@Override
	public String process(String in) {
		String out = in;
		
		Matcher m = Pattern.compile("<s:ql>\\w*</s:ql>").matcher(out);
		if (m.find()) {
			log.info("Processing QuickLinks data tags");
			out = out.replaceAll("<s:ql>url</s:ql>", "\\${s.ql.url}");
			out = out.replaceAll("<s:ql>title</s:ql>", "\\${s.ql.text}");
			
			out = out.replaceAll("<s:ql>domain</s:ql>", "\\${s.result.quickLinks.domain}");
		}
		return out;
	}

}
