package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.ContentOptimiserSelectUrlQuestionFactory;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.MapUtils;

// This component has been temporarily disabled until the output from explain is 
// accurate between queries (See FUN-3541). For now, url-status.cgi is called to  
// determine whether or not a document appears in the results.
//@Component("contentOptimiserSelectDocumentInputProcessor")
@Log4j
public class ContentOptimiserSelectDocument extends AbstractInputProcessor {

    @Override
    public void processInput(SearchTransaction searchTransaction)
        throws InputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.hasQuestion()
                && searchTransaction.getQuestion().getRawInputParameters().containsKey(RequestParameters.EXPLAIN)
                && searchTransaction.getQuestion().getRawInputParameters().containsKey(RequestParameters.CONTENT_OPTIMISER_URL)
                && ! searchTransaction.getQuestion().isExtraSearch()) {
            String url = MapUtils.getFirstString(searchTransaction.getQuestion().getRawInputParameters(), RequestParameters.CONTENT_OPTIMISER_URL, null );
            Map<String,String> m = new HashMap<String,String>();
            log.info("Beginning extra search for content optimiser: " + url);
            m.put(RequestParameters.CONTENT_OPTIMISER_URL, url );
            SearchQuestion q = new ContentOptimiserSelectUrlQuestionFactory().buildQuestion(searchTransaction.getQuestion(),m);
            searchTransaction.addExtraSearch(SearchTransaction.ExtraSearches.CONTENT_OPTIMISER_SELECT_DOCUMENT.toString(), q);
        }

    }

}
