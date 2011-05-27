package com.funnelback.publicui.search.lifecycle.output.processors;

import lombok.extern.apachecommons.Log;

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
@Log
@Component("contentOptimiserOutputProcessor")
public class ContentOptimiser implements OutputProcessor {

	@Autowired
	private UrlCausesFiller filler;
	
	@Autowired
	private HintFactory hintFactory;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction.hasResponse() && searchTransaction.hasQuestion()
				&& searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.EXPLAIN)
				&& !searchTransaction.getQuestion().isExtraSearch()) {
			UrlComparison comparison = new UrlComparison();
		
			filler.consumeResultPacket(comparison, searchTransaction.getResponse().getResultPacket(),hintFactory);
//			if(searchTransaction.get)
		
			
			SearchTransaction selectedDocument = searchTransaction.getExtraSearches().get(SearchTransaction.ExtraSearches.CONTENT_OPTIMISER_SELECT_DOCUMENT.toString());
			if(selectedDocument != null && selectedDocument.hasResponse() && selectedDocument.getResponse().getResultPacket().hasResults()) {
				//log.error(selectedDocument.getResponse().getResultPacket().getResults().get(0).getDisplayUrl());
				filler.setImportantUrl(comparison,selectedDocument.getResponse().getResultPacket());
				filler.fillHints(comparison);
			}
			searchTransaction.getResponse().setUrlComparison(comparison);
		}
	}

}
