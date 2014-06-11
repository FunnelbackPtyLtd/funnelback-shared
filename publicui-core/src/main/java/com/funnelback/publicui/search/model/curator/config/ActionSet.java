package com.funnelback.publicui.search.model.curator.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.context.ApplicationContext;

import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * ActionSet defines a group of actions (which may share a single trigger) and
 * provides convenience methods across the group.
 */
@ToString
public class ActionSet {
    
    /** The actual actions contained within the set */
    @Getter @Setter
    private List<Action> actions = new ArrayList<Action>();
    
    /**
     * Perform any actions within the set should be run in the given phase.
     * @param searchTransaction Current search transaction
     * @param phase current query lifecycle phase where the action is run
     * @param context Modern UI global application context
     */
    public void performActions(SearchTransaction searchTransaction, Phase phase, ApplicationContext context) {
        for (Action action : actions) {
            if (action.runsInPhase(phase, context)) {
                action.performAction(searchTransaction, phase, context);
            }
        }
    }

    /**
     * @param phase current query lifecycle phase where the action is run
     * @param context Modern UI global application context
     * @return true if any of the actions in this set should be run within the specified phase.
     */
    public boolean hasActionForPhase(Phase phase, ApplicationContext context) {
        // Perhaps should keep track of this when things are added to save time
        for (Action action : actions) {
            if (action.runsInPhase(phase, context)) {
                return true;
            }
        }
        return false;
    }
}
