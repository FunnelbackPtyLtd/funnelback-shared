package com.funnelback.publicui.test.mock;

import com.funnelback.publicui.search.lifecycle.input.processors.explore.ExploreQueryGenerator;
import com.funnelback.publicui.search.model.collection.Collection;

public class MockExploreQueryGenerator implements ExploreQueryGenerator {

	@Override
	public String getExploreQuery(Collection c, String url, Integer nbOfTerms) {
		return nbOfTerms + " queries for " + url + " on collection " + c.getId();
	}

}
