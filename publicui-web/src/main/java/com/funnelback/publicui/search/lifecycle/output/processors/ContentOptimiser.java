package com.funnelback.publicui.search.lifecycle.output.processors;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.contentoptimiser.RankingFeatureFactory;
import com.funnelback.contentoptimiser.processors.ContentOptimiserFiller;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.service.anchors.AnchorsFetcher;

/**
 * Process explain output from PADRE and generates relevant data model for the
 * content optimiser
 */
@Log
@Component("contentOptimiserOutputProcessor")
public class ContentOptimiser implements OutputProcessor {

	@Autowired
	private ContentOptimiserFiller filler;
	
	@Autowired
	AnchorsFetcher fetcher;
	
	@Autowired
	private RankingFeatureFactory hintFactory;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction.hasResponse() && searchTransaction.hasQuestion()
				&& searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.EXPLAIN)
				&& !searchTransaction.getQuestion().isExtraSearch()) {
			ContentOptimiserModel comparison = new ContentOptimiserModel();
			log.info("Process output content optimiser has all data");
			filler.consumeResultPacket(comparison, searchTransaction.getResponse().getResultPacket(),hintFactory);
			log.info("Done consuming result packet");
			SearchTransaction selectedDocument = searchTransaction.getExtraSearches().get(SearchTransaction.ExtraSearches.CONTENT_OPTIMISER_SELECT_DOCUMENT.toString());
			if(selectedDocument != null && selectedDocument.hasResponse() && selectedDocument.getResponse().getResultPacket().hasResults()) {
				if(searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL).equals("")) {
					comparison.getMessages().add("No document URL selected.");
					filler.fillHintCollections(comparison);
				}else{
					log.info("setting important url");
					filler.setImportantUrl(comparison,searchTransaction);
					log.info("obtaining anchors");		
					AnchorModel anchors = fetcher.fetchGeneral(comparison.getImportantOne().getDocNum(),comparison.getImportantOne().getCollection());
					log.info("Filling hint texts");
					filler.fillHintCollections(comparison);
					log.info("obtaining content");					
					filler.obtainContentBreakdown(comparison, searchTransaction, comparison.getImportantOne(),anchors);
				
					log.info("done");
				}
			} else {
				if(searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL) == null) {
					comparison.getMessages().add("No document URL selected.");
					filler.fillHintCollections(comparison);
				} else {
					filler.setImportantUrl(comparison,searchTransaction);
					log.info("obtaining anchors");		
					if(comparison.getImportantOne() != null){
						AnchorModel anchors = fetcher.fetchGeneral(comparison.getImportantOne().getDocNum(),comparison.getImportantOne().getCollection());
						log.info("Filling hint texts");
						filler.fillHintCollections(comparison);
						log.info("obtaining content");					
						filler.obtainContentBreakdown(comparison, searchTransaction, comparison.getImportantOne(),anchors);
					} else {
						comparison.getMessages().add("The selected document '" + searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL) + "' was not returned for the query.");
						filler.fillHintCollections(comparison);
					}
	
				}
				
			}
			searchTransaction.getResponse().setUrlComparison(comparison);
		}
	}

}
