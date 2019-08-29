package com.funnelback.publicui.search.model.curator.trigger;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.publicui.search.model.curator.HasNoBeans;
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
 * A trigger which activates only when none of the given 'sub' triggers activate.
 * </p>
 * 
 * <p>
 * This trigger can be used to invert other triggers, e.g. a trigger requiring
 * that a date does not fall in a specified range.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public final class NotAnyTrigger implements Trigger, HasNoBeans {

    /**
     * The list of 'sub' triggers each of which must not activate for this trigger
     * to activate.
     */
    @Setter
    private List<Trigger> triggers = new ArrayList<Trigger>();
    
    public List<Trigger> getTriggers() {
        if(triggers == null) triggers = new ArrayList<>();
        return triggers;
    }

    /**
     * Check each 'sub' trigger in turn to see if it activates on the given
     * searchTransaction, and return true only if none of them activate (otherwise
     * return false).
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        for (Trigger trigger : getTriggers()) {
            if (trigger.activatesOn(searchTransaction)) {
                return false;
            }
        }
        return true;
    }

    /** Configure this trigger and its children (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
        getTriggers().forEach(t -> t.configure(configurer));
    }
}
