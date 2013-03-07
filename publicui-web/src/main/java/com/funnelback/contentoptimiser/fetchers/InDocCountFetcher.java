package com.funnelback.contentoptimiser.fetchers;

import java.util.Map;

import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;

public interface InDocCountFetcher {

    Map<String, Integer> getTermWeights(ContentOptimiserModel comparison, String queryWord, String collectionName);

}
