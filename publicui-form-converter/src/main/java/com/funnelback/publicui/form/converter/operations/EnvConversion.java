package com.funnelback.publicui.form.converter.operations;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:env> tags
 */
@Log
public class EnvConversion implements Operation {

	@Override
	public String process(String in) {
		String out = in;
		
		if (out.contains("<s:env>")) {
			log.info("Processing <s:env> tags");
			out = out.replaceAll("<s:env>SCRIPT_NAME</s:env>", "");
			
			out = out.replaceAll("<s:env>QUERY_STRING</s:env>", "\\${SearchTransaction.question.queryString?html}");
		}
		
		return out;
	}

}
