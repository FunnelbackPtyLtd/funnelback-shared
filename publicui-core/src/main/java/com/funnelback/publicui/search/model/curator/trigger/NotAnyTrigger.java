package com.funnelback.publicui.search.model.curator.trigger;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

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
public class NotAnyTrigger implements Trigger {

    /**
     * The list of 'sub' triggers each of which must not activate for this trigger
     * to activate.
     */
    @Getter
    @Setter
    private List<Trigger> triggers = new ArrayList<Trigger>();

    /**
     * Check each 'sub' trigger in turn to see if it activates on the given
     * searchTransaction, and return true only if none of them activate (otherwise
     * return false).
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        for (Trigger trigger : triggers) {
            if (trigger.activatesOn(searchTransaction)) {
                return false;
            }
        }
        return true;
    }

}
