package com.funnelback.publicui.curator.action;

import java.util.Map;

import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Interface representing a curator action which can be implemented in an
 * external Groovy class (to be subsequently used from within GroovyAction).
 */
public interface GroovyActionInterface {

    /**
     * @param phase
     *            The phase in which the action will be run (if true is
     *            returned).
     * @param properties
     *            Any additional properties specified with the action.
     * @return true if this action should run in the given phase and false
     *         otherwise.
     */
    boolean runsInPhase(Phase phase, Map<String, Object> properties);

    /**
     * Perform the desired action by modifying the passed searchTransaction.
     * 
     * @param searchTransaction
     *            The searchTranscation being processed.
     * @param phase
     *            The phase in which this action is currently being performed.
     * @param properties
     *            Any additional properties specified with the action.
     */
    void performAction(SearchTransaction searchTransaction, Phase phase, Map<String, Object> properties);
}
