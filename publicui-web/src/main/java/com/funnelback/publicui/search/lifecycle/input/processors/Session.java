package com.funnelback.publicui.search.lifecycle.input.processors;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

/**
 * Fetches session data: User, click history, search history, etc.
 * 
 * @since 12.5
 */
@Component("session")
@Log4j
public class Session extends AbstractInputProcessor {

    @Autowired
    @Setter private SearchHistoryRepository searchHistoryRepository;
    
    @Autowired
    @Setter private ResultsCartRepository resultsCartRepository;

    @Override
    public void processInput(SearchTransaction st) throws InputProcessorException {
        if (SearchTransactionUtils.hasSession(st)
            && SearchTransactionUtils.hasCollection(st)
            && st.getQuestion().getCollection()
                .getConfiguration().valueAsBoolean(Keys.ModernUI.SESSION, DefaultValues.ModernUI.SESSION)
            && st.getSession().getSearchUser() != null) {
            
            SearchQuestion q = st.getQuestion();
            SearchSession  s = st.getSession();

            try {
                // Retrieve previous search history
                s.getSearchHistory().addAll(searchHistoryRepository.getSearchHistory(s.getSearchUser(),
                        q.getCollection(), 
                        q.getCollection().getConfiguration().valueAsInt(Keys.ModernUI.Session.SearchHistory.SIZE,
                                DefaultValues.ModernUI.Session.SearchHistory.SIZE)));
                
                // Retrieve previous click history
                s.getClickHistory().addAll(searchHistoryRepository.getClickHistory(s.getSearchUser(),
                        q.getCollection(),
                        q.getCollection().getConfiguration().valueAsInt(Keys.ModernUI.Session.SearchHistory.SIZE,
                                DefaultValues.ModernUI.Session.SearchHistory.SIZE)));
                
                // Retrieve results cart
                st.getSession().getResultsCart().addAll(
                    resultsCartRepository.getCart(st.getSession().getSearchUser(), st.getQuestion().getCollection()));
            } catch (DataAccessException | TransactionException e) {
                log.error("Error while retrieving session data", e);
            }
        }

    }

}
