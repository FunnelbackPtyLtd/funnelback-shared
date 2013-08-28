package com.funnelback.curator.trigger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A trigger which activates only when the current query contains all the words
 * given as triggers.
 * 
 * The query may contain other words without affecting the trigger, and the
 * order of the words within the query is not considered important.
 */
@AllArgsConstructor
@NoArgsConstructor
public class AllQueryWordsTrigger implements Trigger {

    /**
     * The list of words required to be found within the query for this trigger
     * to activate.
     */
    @Getter
    @Setter
    private List<String> triggerWords;

    /**
     * Check the given searchTransaction to see if the query contains all the
     * required words for this trigger. If it does, return true, otherwise
     * false.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        Set<String> queryWords = new HashSet<String>();
        queryWords.addAll(Arrays.asList(searchTransaction.getQuestion().getQuery().split("\\b")));
        return queryWords.containsAll(triggerWords);
    }

}
