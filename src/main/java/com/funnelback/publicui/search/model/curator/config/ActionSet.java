package com.funnelback.publicui.search.model.curator.config;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ActionSet defines a group of actions (which may share a single trigger) and
 * provides convenience methods across the group.
 */
@ToString
@EqualsAndHashCode
public class ActionSet {
    
    /** The actual actions contained within the set */
    @Getter @Setter
    private List<Action> actions = new ArrayList<Action>();
    
    /**
     * Perform any actions within the set should be run in the given phase.
     * @param searchTransaction Current search transaction
     * @param phase current query lifecycle phase where the action is run
     */
    public void performActions(SearchTransaction searchTransaction, Phase phase) {
        for (Action action : actions) {
            if (action.runsInPhase(phase)) {
                action.performAction(searchTransaction, phase);
            }
        }
    }

    /**
     * @param phase current query lifecycle phase where the action is run
     * @return true if any of the actions in this set should be run within the specified phase.
     */
    public boolean hasActionForPhase(Phase phase) {
        // Perhaps should keep track of this when things are added to save time
        for (Action action : actions) {
            if (action.runsInPhase(phase)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Perform any configuration required (e.g. autowiring beans needed by each Action).
     */
    public void configure(Configurer configurer) {
        if(actions != null) {
            actions.forEach(a -> a.configure(configurer));
        }
    }

}
