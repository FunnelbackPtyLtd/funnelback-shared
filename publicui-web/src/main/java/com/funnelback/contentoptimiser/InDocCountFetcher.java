package com.funnelback.contentoptimiser;

import java.util.Map;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;

public interface InDocCountFetcher {

	Map<String, Integer> getTermWeights(ContentOptimiserModel comparison, String queryWord, Collection collection);

}
