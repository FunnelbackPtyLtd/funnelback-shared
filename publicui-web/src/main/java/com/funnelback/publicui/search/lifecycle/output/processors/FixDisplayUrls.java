package com.funnelback.publicui.search.lifecycle.output.processors;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.utils.VFSURLUtils;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Fixes display URLs for results. By default the display URL is identical
 * to the live URL, but sometimes needs some processing.
 */
@Component("fixDisplayUrlsOutputProcessor")
@Log4j
public class FixDisplayUrls implements OutputProcessor {

	@Autowired
	private ConfigRepository configRepository;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
			
			for (Result result : searchTransaction.getResponse().getResultPacket().getResults()) {
				String displayUrl = result.getDisplayUrl();
				Collection resultCollection = configRepository.getCollection(result.getCollection());
				if ( resultCollection == null) {
					log.warn("Invalid collection '" + result.getCollection() + "' for result '" + result + "'");
					continue;
				}
				
				switch (resultCollection.getType()) {
				
				case local:
				case filecopy:
				case connector:
					displayUrl = VFSURLUtils.vfsUrlToSystemUrl(displayUrl);
					break;
				default:
					// Do nothing
				}
				
				log.debug("Display URL transformed from '"+result.getDisplayUrl()+"' to '"+displayUrl+"'");
				result.setDisplayUrl(displayUrl);
			}
		
		}


	}

}
