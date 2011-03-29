package com.funnelback.publicui.search.lifecycle.output.processors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.QuickLinks.QuickLink;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Processes quick links output:
 * <ul>
 * <li>Restores 'http://' prefixes</li>
 * </ul>
 * 
 */
@Component("quickLinksOutputProcessor")
public class QuickLinks implements OutputProcessor {
	
	@Override
	public void process(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
			for (Result r : searchTransaction.getResponse().getResultPacket().getResults()) {

				if (r.getQuickLinks() != null) {
					for (QuickLink ql : r.getQuickLinks().getQuickLinks()) {
						if (ql.getUrl() != null) {
							
							// Try to find a colon between the first dot, to assess
							// the presence of a scheme in the URL.
							int dot = ql.getUrl().indexOf('.');
							if (dot == -1) {
								// In case no dot: http://server/page
								dot = ql.getUrl().length();
							}
							
							if (ql.getUrl().substring(0, dot).indexOf(':') < 0) {
								ql.setUrl("http://" + ql.getUrl());
							}
						}
					}
				}
			}
		}

	}
}
