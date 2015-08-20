package com.funnelback.publicui.search.model.curator.trigger;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * A trigger which activates only when the current query contains all the words
 * given as triggers.
 * </p>
 * 
 * <p>
 * The query may contain other words without affecting the trigger, and the
 * order of the words within the query is not considered important.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of={"triggerWords"})
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
        queryWords.addAll(Arrays.asList(searchTransaction.getQuestion().getQuery().toLowerCase().split("\\b")));
        
        List<String> lowercasedTriggerWords = new ArrayList<String>();
        for (String triggerWord : triggerWords) {
            lowercasedTriggerWords.add(triggerWord.toLowerCase());
        }
        
        return queryWords.containsAll(lowercasedTriggerWords);
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
