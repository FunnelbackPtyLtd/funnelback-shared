package com.funnelback.publicui.search.lifecycle.data.fetchers;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

@Component("dummyDataFetcher")
@Log
public class Dummy implements DataFetcher {

	@Override
	public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
		log.debug("Fetching data...");		
	}

}
