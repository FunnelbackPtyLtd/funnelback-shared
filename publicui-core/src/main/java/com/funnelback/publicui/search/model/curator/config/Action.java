package com.funnelback.publicui.search.model.curator.config;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Action defines an 'action' performed by the curator system to modify search
 * results in some way.
 * 
 * Actions may run in an input or output phase (or both) to modify either the
 * request to be sent to padre or the packet of results to be returned to the
 * user.
 * 
 * Implementing subclasses must perform the desired action by modifying the
 * passed searchTransaction object within the performAction method.
 * 
 * NOTE: To avoid displaying the full package name for the implementing class in
 * the curator config file add the implementing class to the aliasedActions
 * array in
 * com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource
 */
public interface Action {

    /**
     * Phases in which an action may be executed
     */
    public enum Phase {
        INPUT, OUTPUT
    }

    /**
     * Perform the defined action by modifying the searchTransaction object.
     * 
     * Some actions may wish to behave differently depending on the current
     * phase of processing.
     */
    public void performAction(SearchTransaction searchTransaction, Phase phase);

    /**
     * Return true if this action should be run in the given phase, otherwise
     * return false.
     */
    public boolean runsInPhase(Phase phase);
}
