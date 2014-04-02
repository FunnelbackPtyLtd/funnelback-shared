package com.funnelback.publicui.search.model.curator.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An association between a curator {@link Trigger} and the {@link ActionSet}
 * to perform for this trigger.
 * 
 * @since 13.4
 */
@NoArgsConstructor
public class TriggerActions {
    
    /**
     * {@link Trigger} associated to the {@link ActionSet}
     */
    @Getter
    @Setter
    private Trigger trigger;
    
    /**
     * Set of actions for the {@link Trigger}
     */
    @Getter
    @Setter
    private ActionSet actions;

}
