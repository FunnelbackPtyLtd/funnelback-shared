package com.funnelback.publicui.search.model.curator.config;

import org.springframework.context.ApplicationContext;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * Triggers control when curator actions are performed (or not).
 * </p>
 * 
 * <p>
 * Implementing classes must define, based on the current search transaction,
 * whether the trigger activates or not.
 * </p>
 * 
 * <p>
 * NOTE: To avoid displaying the full package name for the implementing class in
 * the curator config file add the implementing class to the aliasedTriggers
 * array in publicui-web's
 * com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource
 * </p>
 */
public interface Trigger {

    /**
     * Return true if this trigger should activate on the given
     * searchTransaction, and false otherwise.
     */
    public boolean activatesOn(SearchTransaction searchTransaction, ApplicationContext context);

}
