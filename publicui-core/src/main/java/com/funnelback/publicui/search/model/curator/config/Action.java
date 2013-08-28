package com.funnelback.publicui.search.model.curator.config;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * Action defines an 'action' performed by the curator system to modify search
 * results in some way.
 * </p>
 * 
 * <p>
 * Actions may run in an input or output phase (or both) to modify either the
 * request to be sent to padre or the packet of results to be returned to the
 * user.
 * </p>
 * 
 * <p>
 * Implementing subclasses must perform the desired action by modifying the
 * passed searchTransaction object within the performAction method.
 * </p>
 * 
 * <p>
 * NOTE: To avoid displaying the full package name for the implementing class in
 * the curator config file add the implementing class to the aliasedActions
 * array in publicui-web's
 * com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource
 * </p>
 */
public interface Action {

    /**
     * Phases in which an action may be executed
     */
    public enum Phase {
        INPUT, OUTPUT
    }

    /**
     * <p>
     * Perform the defined action by modifying the searchTransaction object.
     * <p>
     * 
     * @param phase
     *            The phase being processed (Some actions may wish to behave
     *            differently depending on the current phase of processing).
     */
    public void performAction(SearchTransaction searchTransaction, Phase phase);

    /**
     * @return true if this action should be run in the given phase, otherwise
     *         return false.
     */
    public boolean runsInPhase(Phase phase);
}
