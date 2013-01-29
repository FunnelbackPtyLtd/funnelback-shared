package com.funnelback.publicui.test.mock;

import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.Suggestion;
import com.funnelback.publicui.search.service.Suggester;

public class MockSuggester implements Suggester {

	@Override
	public List<Suggestion> suggest(Collection collection, String profileId,
			String partialQuery, int numSuggestions, Sort sort) {
		// TODO Auto-generated method stub
		return null;
	}

}
