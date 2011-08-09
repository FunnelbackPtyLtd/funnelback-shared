package com.funnelback.publicui.search.lifecycle.input.processors;

import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.FacetedNavigationQuestionFactory;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

@Component("multiFacetedNavigationInputProcessor")
public class MultiFacetedNavigation implements InputProcessor {

	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasCollection(searchTransaction)
				&& Config.isTrue(searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.ModernUI.FULL_FACETS_LIST))
				&& ! searchTransaction.getQuestion().isExtraSearch()) {
			SearchQuestion q = new FacetedNavigationQuestionFactory().buildQuestion(searchTransaction.getQuestion(), null);
			searchTransaction.addExtraSearch(SearchTransaction.ExtraSearches.FACETED_NAVIGATION.toString(), q);
		}
	}

}
