package com.funnelback.publicui.search.lifecycle.output.processors;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.url.VFSURLUtils;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;

import lombok.extern.log4j.Log4j2;

/**
 * Fixes display URLs for results. By default the display URL is identical
 * to the live URL, but sometimes needs some processing.
 */
@Component("fixDisplayUrlsOutputProcessor")
@Log4j2
public class FixDisplayUrls extends AbstractOutputProcessor {

    @Autowired
    private ConfigRepository configRepository;
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (SearchTransactionUtils.hasResults(searchTransaction)) {
            
            for (Result result : searchTransaction.getResponse().getResultPacket().getResults()) {
                String displayUrl = result.getDisplayUrl();
                
                // Most of the time the result will be coming from the same collection as
                // the question, except for meta collections
                Collection resultCollection = searchTransaction.getQuestion().getCollection();
                if (! searchTransaction.getQuestion().getCollection().getId().equals(result.getCollection())) {
                    resultCollection = configRepository.getCollection(result.getCollection());
                }
                
                if ( resultCollection == null) {
                    log.warn("Invalid collection '" + result.getCollection() + "' for result '" + result + "'");
                    continue;
                }
                
                try {
                    switch (resultCollection.getType()) {
                    
                    case local:
                    case filecopy:
                        displayUrl = VFSURLUtils.vfsUrlToSystemUrl(URI.create(displayUrl));
                        break;
                    default:
                        // Do nothing
                    }
                    
                    log.debug("Display URL transformed from '"+result.getDisplayUrl()+"' to '"+displayUrl+"'");
                    result.setDisplayUrl(displayUrl);
                } catch (IllegalArgumentException iae) {
                    log.warn("Error converting URL {}. This may cause further issues"
                        + " as the URL will not have been converted properly for search results display",
                        displayUrl, iae);
                }
            }
        }
    }

}
