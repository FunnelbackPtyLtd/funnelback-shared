package com.funnelback.contentoptimiser.fetchers;

import java.io.IOException;

import com.funnelback.contentoptimiser.processors.impl.BldInfoStats;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;

public interface BldInfoStatsFetcher {

	BldInfoStats fetch(ContentOptimiserModel model,Collection collection) throws IOException;

}
