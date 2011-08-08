package com.funnelback.publicui.search.lifecycle.output.processors;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>HTML Encodes summaries returned by PADRE because they
 * can contain decoded HTML entities (See FUN-3530).</p>
 * 
 * <p>Only encode specific unsafe characters that could generate
 * invalid HTML. Other entities (accented letters, etc.)</p>
 */
@Component("htmlEncodeSummaries")
public class HTMLEncodeSummaries implements OutputProcessor {
	
	private static final String[] UNSAFE_CHARS = {"\\", "\"", "'", "<", ">", "&"};
	private static final String[] SAFE_REPLACEMENT = {"&#92;", "&quot;", "&apos;", "&lt;", "&gt;", "&amp;"};

	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
			for(Result r: searchTransaction.getResponse().getResultPacket().getResults()) {
				if (r.getSummary() != null) {
					r.setSummary(StringUtils.replaceEach(r.getSummary(), UNSAFE_CHARS, SAFE_REPLACEMENT));
				}
			}
		}
	}
}
