package com.funnelback.publicui.search.service;

import java.util.List;

import com.funnelback.dataapi.connector.padre.suggest.SuggestQuery.Sort;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * Get query suggestions from the index
 */
public interface Suggester {

    /**
     * Get query suggestions
     * @param collection Collection to get suggestions from
     * @param profileId Profile to get suggestions from
     * @param partialQuery Partial query to get suggestions for
     * @param numSuggestions Max. number of suggestions to return
     * @param sort How to sort returned suggestions
     * @return A list of suggestions, possibly empty if there are none.
     */
    public List<Suggestion> suggest(Collection collection, String profileId, String partialQuery,
        int numSuggestions, Sort sort);
    
}
