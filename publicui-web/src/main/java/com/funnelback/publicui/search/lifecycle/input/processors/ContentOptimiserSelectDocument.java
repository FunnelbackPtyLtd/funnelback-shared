package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.ContentOptimiserSelectUrlQuestionFactory;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

@Log
@Component("contentOptimiserSelectDocumentInputProcessor")
public class ContentOptimiserSelectDocument implements InputProcessor {

	@Override
	public void processInput(SearchTransaction searchTransaction)
			throws InputProcessorException {
		if (SearchTransactionUtils.hasCollection(searchTransaction)
				&& searchTransaction.hasQuestion()
				&& searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.EXPLAIN)
				&& searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.OPTIMISER_URL)
				&& ! searchTransaction.getQuestion().isExtraSearch()) {
			Map<String,String> m = new HashMap<String,String>();
			log.info("Beginning extra search for content optimiser: " +  searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL ) );
			m.put(RequestParameters.OPTIMISER_URL, searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL ) );
			SearchQuestion q = new ContentOptimiserSelectUrlQuestionFactory().buildQuestion(searchTransaction.getQuestion(),m);
			searchTransaction.addExtraSearch(SearchTransaction.ExtraSearches.CONTENT_OPTIMISER_SELECT_DOCUMENT.toString(), q);
		}

	}

}
