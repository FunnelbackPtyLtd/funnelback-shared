package com.funnelback.publicui.test.mock;

import java.util.List;

import com.funnelback.dataapi.connector.padre.suggest.SuggestQuery.Sort;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.Suggester;

public class MockSuggester implements Suggester {

    @Override
    public List<Suggestion> suggest(Collection collection, String profileId,
            String partialQuery, int numSuggestions, Sort sort, double alpha, String category) {
        return null;
    }

}
