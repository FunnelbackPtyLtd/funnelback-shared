package com.funnelback.publicui.search.model.curator.config;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Triggers control when curator actions are performed (or not).
 * 
 * Implementing classes must define, based on the current search transaction,
 * whether the trigger activates or not.
 * 
 * NOTE: To avoid displaying the full package name for the implementing class in
 * the curator config file add the implementing class to the aliasedTriggers
 * array in
 * com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource
 */
public interface Trigger {

    /**
     * Return true if this trigger should activate on the given
     * searchTransaction, and false otherwise.
     */
    public boolean activatesOn(SearchTransaction searchTransaction);

}
