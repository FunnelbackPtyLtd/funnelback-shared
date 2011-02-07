package com.funnelback.publicui.search.lifecycle.output.processors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Category;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Process PADRE contextual navigation output:
 * - Clears up "more_link"s to remove the hardcoded script name /search/padre-sw.cgi
 * 
 *
 */
@Component("contextualNavigationOutputProcessor")
public class ContextualNavigation implements OutputProcessor {

	@Override
	public void process(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResponse(searchTransaction)
				&& searchTransaction.getResponse().hasResultPacket()
				&& searchTransaction.getResponse().getResultPacket().getContextualNavigation() != null) {
			
			for (Category category: searchTransaction.getResponse().getResultPacket().getContextualNavigation().getCategories()) {
				// Strips any hardcoded URL such as "/search/padre-sw.cgi?..."
				if (category.getMoreLink() != null) {
					category.setMoreLink(category.getMoreLink().substring(category.getMoreLink().indexOf("?")));
				}
				if (category.getFewerLink() != null) {
					category.setFewerLink(category.getFewerLink().substring(category.getFewerLink().indexOf("?")));
				}
			}
			
		}

	}

}
