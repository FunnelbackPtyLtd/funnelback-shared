package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Set;
import java.util.Map.Entry;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.contentoptimiser.RankingFeatureFactory;
import com.funnelback.contentoptimiser.UrlStatus;
import com.funnelback.contentoptimiser.fetchers.UrlStatusFetcher;
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
	AnchorsFetcher anchorsFetcher;
	
	@Autowired
	UrlStatusFetcher urlStatusFetcher;
	
	@Autowired
	private RankingFeatureFactory hintFactory;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction.hasResponse() && searchTransaction.hasQuestion()
				&& searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.EXPLAIN)
				&& !searchTransaction.getQuestion().isExtraSearch()) {
			ContentOptimiserModel comparison = new ContentOptimiserModel();
			log.debug("Process output content optimiser has all data");
			filler.consumeResultPacket(comparison, searchTransaction.getResponse().getResultPacket(),hintFactory);
			log.debug("Done consuming result packet");
			
			if(!"".equals(searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.CONTENT_OPTIMISER_URL)) && searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.CONTENT_OPTIMISER_URL) != null) {
				// if there is an optimiser URL, look it up with the URL status tool
				UrlStatus status = urlStatusFetcher.fetch(searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.CONTENT_OPTIMISER_URL),searchTransaction.getQuestion().getCollection().getId());
				if(status != null && ! status.isAvailable() && ! status.getError().startsWith("Unsupported collection type")) {
					comparison.getMessages().add("Information about the selected URL was unavailable due to the following message from the crawler: \"" + status.getError() + "\". Ask your administrator for more information.");
				}				
				
				// if there was, we should try and find it anyway
				filler.setImportantUrl(comparison,searchTransaction);
				log.debug("Filling hint texts");
				filler.fillHintCollections(comparison);
			
				if(comparison.getImportantOne() != null){
					log.debug("obtaining anchors");		
					AnchorModel anchors = anchorsFetcher.fetchGeneral(comparison.getImportantOne().getDocNum(),comparison.getImportantOne().getCollection());

					log.debug("obtaining content");					
					filler.obtainContentBreakdown(comparison, searchTransaction, comparison.getImportantOne(),anchors,searchTransaction.getResponse().getResultPacket().getStemmedEquivs());
				}
			} else {
				// if there isn't an optimiser URL, note that we didn't find anything
				comparison.getMessages().add("No document URL selected.");
				filler.fillHintCollections(comparison);
			}
			searchTransaction.getResponse().setUrlComparison(comparison);
		}
	}

}
