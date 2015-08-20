package com.funnelback.publicui.search.model.curator.trigger;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

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
 * A trigger which activates only when at least one of the 'sub' triggers
 * activates.
 * </p>
 * 
 * <p>
 * This trigger can be used to combine the effects of other triggers, e.g. a
 * trigger requiring either some query word or some date range.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class OrTrigger implements Trigger {

    /**
     * The list of 'sub' triggers one of which must activate for this trigger to
     * activate.
     */
    @Getter
    @Setter
    private List<Trigger> triggers = new ArrayList<Trigger>();

    /**
     * Check each 'sub' trigger in turn to see if it activates on the given
     * searchTransaction, and return true only if one of them does (otherwise
     * return false).
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        for (Trigger trigger : triggers) {
            if (trigger.activatesOn(searchTransaction)) {
                return true;
            }
        }
        return false;
    }

    /** Configure this trigger and its children (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
        for (Trigger trigger : triggers) {
            trigger.configure(configurer);
        }
    }
}
