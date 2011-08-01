package com.funnelback.publicui.search.lifecycle.output.processors;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>HTML Encodes summaries returned by PADRE because they
 * can contain decoded HTML entities (See FUN-3530).
 */
@Component("htmlEncodeSummaries")
public class HTMLEncodeSummaries implements OutputProcessor {

	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
			for(Result r: searchTransaction.getResponse().getResultPacket().getResults()) {
				if (r.getSummary() != null) {
					r.setSummary(HtmlUtils.htmlEscape(r.getSummary()));
				}
			}
		}
	}
}
