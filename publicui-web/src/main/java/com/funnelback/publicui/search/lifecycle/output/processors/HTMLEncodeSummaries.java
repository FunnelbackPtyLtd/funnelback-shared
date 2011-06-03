package com.funnelback.publicui.search.lifecycle.output.processors;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>HTML Encodes summaries returned by PADRE, then re-decodes
 * the highlight tags (&lt;b&gt;).</p>
 * 
 * <p>This can be removed once that FUN-3530 is fixed.</p>
 *
 */
@Component("htmlEncodeSummaries")
public class HTMLEncodeSummaries implements OutputProcessor {

	private static final String PADRE_HIGHLIGHT_TAG = "strong";
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
			for(Result r: searchTransaction.getResponse().getResultPacket().getResults()) {
				if (r.getSummary() != null) {
					r.setSummary(HtmlUtils.htmlEscape(r.getSummary())
							.replace("&lt;"+PADRE_HIGHLIGHT_TAG+"&gt;", "<"+PADRE_HIGHLIGHT_TAG + ">")
							.replace("&lt;/"+PADRE_HIGHLIGHT_TAG+"&gt;", "</"+PADRE_HIGHLIGHT_TAG+">"));
				}
			}
		}
	}
}
