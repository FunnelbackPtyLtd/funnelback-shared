package com.funnelback.contentoptimiser;

import java.util.Map;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;

public interface TermWeightFetcher {

	Map<String, Integer> getTermWeights(UrlComparison comparison, String queryWord, Collection collection);

}
