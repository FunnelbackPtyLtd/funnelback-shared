package com.funnelback.publicui.search.lifecycle.output.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.contentoptimiser.HintFactory;
import com.funnelback.contentoptimiser.UrlCausesFiller;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;

/**
 * Process explain output from PADRE and generates relevant data model for the
 * content optimiser
 */
@Component("contentOptimiserOutputProcessor")
public class ContentOptimiser implements OutputProcessor {

	@Autowired
	private UrlCausesFiller filler;
	
	@Autowired
	private HintFactory hintFactory;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction.hasResponse() && searchTransaction.hasQuestion()
				&& searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.EXPLAIN)) {
			UrlComparison comparison = new UrlComparison();
		
			filler.consumeResultPacket(comparison, searchTransaction.getResponse().getResultPacket(),hintFactory);
			filler.setImportantUrl("", comparison, searchTransaction.getResponse().getResultPacket());
			filler.fillHints(comparison);
			
			searchTransaction.getResponse().setUrlComparison(comparison);
		}
	}

}
