package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Date;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetScope;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

/**
 * Saves the current search in the user search history.
 * 
 * @since 12.5
 */
@Component("searchHistoryOutputProcessor")
@Log4j2
public class SearchHistory extends AbstractOutputProcessor {

    @Autowired
    @Setter private SearchHistoryRepository searchHistoryRepository;

    @Override
    public void processOutput(SearchTransaction st)
        throws OutputProcessorException {
        
        if (!SearchTransactionUtils.hasQuestion(st)) {
            return;
        }
        
        if(!st.getQuestion().getLogQuery().orElse(true)) {
            log.trace("Search question has logQuery set to false so search history will not be saved.");
            return;
        }

        if (SearchTransactionUtils.hasResponse(st)
            && SearchTransactionUtils.hasSession(st)
            && st.getQuestion().getCurrentProfileConfig().get(FrontEndKeys.ModernUi.Session.SESSION)
            && st.getSession().getSearchUser() != null
            && st.getSession().getSearchUser().getId() != null ) {
        
            SearchResponse r = st.getResponse();
            SearchQuestion q = st.getQuestion();

            // Save current search
            if (r.getResultPacket() != null && r.getResultPacket().getError() == null) {
                com.funnelback.publicui.search.model.transaction.session.SearchHistory h =
                    new com.funnelback.publicui.search.model.transaction.session.SearchHistory();
                h.setUserId(st.getSession().getSearchUser().getId());
                h.setCurrStart(r.getResultPacket().getResultsSummary().getCurrStart());
                h.setNumRanks(r.getResultPacket().getResultsSummary().getNumRanks());
                h.setOriginalQuery(q.getOriginalQuery());
                h.setQueryAsProcessed(r.getResultPacket().getQueryAsProcessed());
                h.setSearchDate(new Date());
                h.setTotalMatching(r.getResultPacket().getResultsSummary().getTotalMatching());
                h.setCollection(q.getCollection().getId());
                try {
                    String url = q.getInputParameterMap()
                        .get(PassThroughEnvironmentVariables.Keys.REQUEST_URL.toString());
                    // Convert 'facetScope' parameter into actual facets parameters
                    h.setSearchParams(FacetScope.convertFacetScopeParameters(new URL(url).getQuery()));
                    
                    searchHistoryRepository.saveSearch(h);
                } catch (MalformedURLException e) {
                    log.warn("Couldn't parse search URL", e);
                } catch (DataAccessException | TransactionException e) {
                    log.error("Error while saving search history: {}", h,  e);
                }
            }
        }

    }
}
