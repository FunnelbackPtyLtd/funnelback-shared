package com.funnelback.publicui.search.lifecycle.data;

import com.funnelback.publicui.search.lifecycle.LifecycleProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Fetches data for a specific {@link SearchTransaction}
 */
public interface DataFetcher extends LifecycleProcessor {

    public void fetchData(final SearchTransaction searchTransaction) throws DataFetchException;
    
}
