package com.funnelback.publicui.form.converter.operations;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:QueryClean> tags
 */
@Log
public class QueryCleanConversion implements Operation {

	@Override
	public String process(String in) {
		String out = in;
		
		if (out.contains("<s:QueryClean>")) {
			log.info("Processing <s:QueryClean> tags");
			out = out.replaceAll("<s:QueryClean>[^<]*</s:QueryClean>", "\\${SearchTransaction.response.resultPacket.queryCleaned}");
		}
		
		return out;
	}

}
