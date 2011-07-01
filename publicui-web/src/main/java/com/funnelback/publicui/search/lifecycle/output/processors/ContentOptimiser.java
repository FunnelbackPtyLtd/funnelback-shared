package com.funnelback.publicui.search.lifecycle.output.processors;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.contentoptimiser.HintFactory;
import com.funnelback.contentoptimiser.UrlCausesFiller;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;
import com.funnelback.publicui.search.service.anchors.AnchorsFetcher;

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
	AnchorsFetcher fetcher;
	
	@Autowired
	private HintFactory hintFactory;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction.hasResponse() && searchTransaction.hasQuestion()
				&& searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.EXPLAIN)
				&& !searchTransaction.getQuestion().isExtraSearch()) {
			UrlComparison comparison = new UrlComparison();
			log.info("Process output content optimiser has all data");
			filler.consumeResultPacket(comparison, searchTransaction.getResponse().getResultPacket(),hintFactory);
			log.info("Done consuming result packet");
			SearchTransaction selectedDocument = searchTransaction.getExtraSearches().get(SearchTransaction.ExtraSearches.CONTENT_OPTIMISER_SELECT_DOCUMENT.toString());
			if(selectedDocument != null && selectedDocument.hasResponse() && selectedDocument.getResponse().getResultPacket().hasResults()) {
				if(searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL).equals("")) {
					comparison.getMessages().add("No document URL selected.");
				}else{
					log.info("setting important url");
					filler.setImportantUrl(comparison,searchTransaction);
					log.info("obtaining content");
					AnchorModel anchors = fetcher.fetchGeneral(selectedDocument.getResponse().getResultPacket().getResults().get(0).getDocNum(),searchTransaction.getQuestion().getCollection());
					
					filler.obtainContentBreakdown(comparison, searchTransaction, selectedDocument.getResponse().getResultPacket(),anchors);
					log.info("obtaining anchors");		
				
					log.info("done");
				}
			} else {
				if(searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL) == null) {
					comparison.getMessages().add("No document URL selected.");
				} else {
					comparison.getMessages().add("The selected document '" + searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL) + "' was not returned for the query.");
				}
			}
			filler.fillHintCollections(comparison);
			searchTransaction.getResponse().setUrlComparison(comparison);
		}
	}

}
