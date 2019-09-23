package com.funnelback.publicui.search.lifecycle;

import java.util.Optional;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * <p>Processes a {@link SearchTransaction}. Runs the different
 * processors and data fetchers.<p>
 * 
 * <p>See the Spring XML configuration file to find out where the default
 * implementation is.</p>
 */
public interface SearchTransactionProcessor {

    /**
     * Processes a {@link SearchQuestion} into a {@link SearchTransaction}
     * @param q {@link SearchQuestion} to process
     * @param user Current user performing the search, might be null
     * @return The processed {@link SearchTransaction}
     */
    public SearchTransaction process(SearchQuestion q, SearchUser user, Optional<String> extraSearchName, Optional<SearchTransaction> parentSearchTransaction);
    
}
