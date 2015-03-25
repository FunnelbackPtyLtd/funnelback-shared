package com.funnelback.publicui.search.model.curator.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
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
 * com.funnelback.publicui.search.service.resource.impl.CuratorConfigResource
 * </p>
 * 
 * @since 13.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(TriggerTypeIdResolver.class)
public interface Trigger {

    /**
     * @param searchTransaction Current search transaction
     * @return true if this trigger should activate on the given
     * searchTransaction, and false otherwise.
     */
    public boolean activatesOn(SearchTransaction searchTransaction);
    
    /**
     * Perform any configuration required (e.g. autowiring beans needed by the Trigger).
     */
    public void configure(Configurer configurer);

}
