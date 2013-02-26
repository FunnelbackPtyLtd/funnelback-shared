package com.funnelback.publicui.search.lifecycle.data;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Base class for {@link DataFetcher}s
 * 
 * @since 12.4
 */
public abstract class AbstractDataFetcher implements DataFetcher {

	@Override
	public String getId() {
		return this.getClass().getSimpleName();
	}

	@Override
	public abstract void fetchData(SearchTransaction searchTransaction)
			throws DataFetchException;

}
