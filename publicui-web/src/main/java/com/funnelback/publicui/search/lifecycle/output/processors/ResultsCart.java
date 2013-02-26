package com.funnelback.publicui.search.lifecycle.output.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ResultsCartRepository;

/**
 * Retrieve the current results cart of the user
 * 
 * @since 12.4
 */
@Component("resultsCartOutputProcessor")
public class ResultsCart extends AbstractOutputProcessor {

	@Autowired
	private ResultsCartRepository repository;
	
	@Override
	public void processOutput(SearchTransaction st)
			throws OutputProcessorException {

		if (SearchTransactionUtils.hasQuestion(st)
				&& SearchTransactionUtils.hasResponse(st)
				&& st.getQuestion().getSearchUser() != null) {
			
			st.getResponse().getResultsCart().putAll(repository.getCart(st.getQuestion().getSearchUser(), st.getQuestion().getCollection()));
		}

	}

}
