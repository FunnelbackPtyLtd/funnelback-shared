package com.funnelback.publicui.search.lifecycle.output.processors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

/**
 * Saves the current search in the user search history, and populates
 * the model with the previous history.
 * 
 * @since 12.0
 */
@Component("searchHistoryOutputProcessor")
@Log4j
public class SearchHistory extends AbstractOutputProcessor {

	@Autowired
	private SearchHistoryRepository repository;

	@Override
	public void processOutput(SearchTransaction st)
			throws OutputProcessorException {

		if (SearchTransactionUtils.hasQuestion(st)
				&& SearchTransactionUtils.hasResponse(st)
				&& st.getQuestion().getSearchUser() != null) {
		
			SearchResponse r = st.getResponse();
			SearchQuestion q = st.getQuestion();

			// Save current search
			if (r.getResultPacket() != null && r.getResultPacket().getError() == null) {
				com.funnelback.publicui.search.model.transaction.session.SearchHistory h = new com.funnelback.publicui.search.model.transaction.session.SearchHistory();
				h.setCurrStart(r.getResultPacket().getResultsSummary().getCurrStart());
				h.setNumRanks(r.getResultPacket().getResultsSummary().getNumRanks());
				h.setOriginalQuery(q.getOriginalQuery());
				h.setQueryAsProcessed(r.getResultPacket().getQueryAsProcessed());
				h.setSearchDate(new Date());
				h.setTotalMatching(r.getResultPacket().getResultsSummary().getTotalMatching());
				h.setCollectionId(q.getCollection().getId());
				try {
					h.setSearchUrl(new URL(q.getInputParameterMap().get(PassThroughEnvironmentVariables.Keys.REQUEST_URL.toString())));
				} catch (MalformedURLException mue) {
					log.warn("Couldn't parse search URL", mue);
				}
				
				repository.saveSearch(q.getSearchUser(), h, q.getCollection());
			}

			// Retrieve previous history
			r.getSearchHistory().addAll(repository.getSearchHistory(q.getSearchUser(),
					q.getCollection(), 
					q.getCollection().getConfiguration().valueAsInt(Keys.ModernUI.Session.SEARCH_HISTORY_SIZE,
							DefaultValues.ModernUI.Session.SEARCH_HISTORY_SIZE)));
			
			r.getClickHistory().addAll(repository.getClickHistory(q.getSearchUser(),
					q.getCollection(),
					q.getCollection().getConfiguration().valueAsInt(Keys.ModernUI.Session.SEARCH_HISTORY_SIZE,
							DefaultValues.ModernUI.Session.SEARCH_HISTORY_SIZE)));

		}

	}
}
