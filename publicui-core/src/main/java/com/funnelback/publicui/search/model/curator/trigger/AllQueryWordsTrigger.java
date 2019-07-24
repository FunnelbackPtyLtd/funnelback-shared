package com.funnelback.publicui.search.model.curator.trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    @Setter
    private List<String> triggerWords = new ArrayList<>();
    
    public List<String> getTriggerWords() {
        if(triggerWords == null) triggerWords = new ArrayList<>();
        return triggerWords;
    }

    /**
     * Check the given searchTransaction to see if the query contains all the
     * required words for this trigger. If it does, return true, otherwise
     * false.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        Set<String> queryWords = new HashSet<String>();
        queryWords.addAll(Arrays.asList(Trigger.queryToMatchAgainst(searchTransaction).toLowerCase().split("\\b")));
        
        List<String> lowercasedTriggerWords = new ArrayList<String>();
        for (String triggerWord : getTriggerWords()) {
            // Skip any 'empty' words - FUN-8734
            String triggerWordTrimmed = triggerWord.trim();
            if (triggerWordTrimmed.length() > 0) {
                lowercasedTriggerWords.add(triggerWordTrimmed.toLowerCase());
            }
        }
        
        return queryWords.containsAll(lowercasedTriggerWords);
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
