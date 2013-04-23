package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

/**
 * Fetches session data: User, click history, search history, etc.
 * 
 * @since 12.5
 */
@Component("session")
public class Session extends AbstractInputProcessor {

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    @Autowired
    private ResultsCartRepository resultsCartRepository;

    @Override
    public void processInput(SearchTransaction st) throws InputProcessorException {
        if (SearchTransactionUtils.hasSession(st)
            && st.getSession().getSearchUser() != null) {
            
            SearchQuestion q = st.getQuestion();
            SearchSession  s = st.getSession();

            // Retrieve previous search history
            s.getSearchHistory().addAll(searchHistoryRepository.getSearchHistory(s.getSearchUser(),
                    q.getCollection(), 
                    q.getCollection().getConfiguration().valueAsInt(Keys.ModernUI.Session.SEARCH_HISTORY_SIZE,
                            DefaultValues.ModernUI.Session.SEARCH_HISTORY_SIZE)));
            
            // Retrieve previous click history
            s.getClickHistory().addAll(searchHistoryRepository.getClickHistory(s.getSearchUser(),
                    q.getCollection(),
                    q.getCollection().getConfiguration().valueAsInt(Keys.ModernUI.Session.SEARCH_HISTORY_SIZE,
                            DefaultValues.ModernUI.Session.SEARCH_HISTORY_SIZE)));
            
            // Retrieve results cart
            st.getSession().getResultsCart().putAll(
                resultsCartRepository.getCart(st.getSession().getSearchUser(), st.getQuestion().getCollection()));
        }

    }

}
