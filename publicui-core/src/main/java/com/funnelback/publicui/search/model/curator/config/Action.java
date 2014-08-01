package com.funnelback.publicui.search.model.curator.config;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonTypeIdResolver;

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
 * com.funnelback.publicui.search.service.resource.impl.CuratorConfigResource
 * </p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(ActionTypeIdResolver.class)
public interface Action {

    /**
     * Phases in which an action may be executed
     */
    public enum Phase {
        /** Input phase, i.e. before the query is run */
        INPUT,
        /** Output phase, i.e. after the query is run */
        OUTPUT
    }

    /**
     * <p>
     * Perform the defined action by modifying the searchTransaction object.
     * <p>
     * 
     * @param searchTransaction Current search transaction
     * @param phase
     *            The phase being processed (Some actions may wish to behave
     *            differently depending on the current phase of processing).
     * @param context Modern UI global application context
     */
    public void performAction(SearchTransaction searchTransaction, Phase phase);

    /**
     * @param phase
     *            The phase being processed (Some actions may wish to behave
     *            differently depending on the current phase of processing).
     * @param context Modern UI global application context
     * @return true if this action should be run in the given phase, otherwise
     *         return false.
     */
    public boolean runsInPhase(Phase phase);
    
    /**
     * Perform any configuration required (e.g. autowiring beans needed by the Action).
     */
    public void configure(Configurer configurer);
}
