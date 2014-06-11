package com.funnelback.publicui.search.model.curator.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * An association between a curator {@link Trigger} and the {@link ActionSet}
 * to perform for this trigger.
 * 
 * This is known as a RuleSet in the Admin UI interface.
 * 
 * @since 13.4
 */
@NoArgsConstructor
@ToString
public class TriggerActions {
    
    /**
     * Name for the TriggerAction (called a RuleSet in the user interface)
     */
    @Getter
    @Setter
    private String name;    
    
    /**
     * {@link Trigger} associated to the {@link ActionSet}
     */
    @Getter
    @Setter
    private Trigger trigger;
    
    /**
     * Set of actions for the {@link Trigger}
     */
    @Setter
    private List<Action> actions = new ArrayList<Action>();

    /**
     * Get the set of actions for this trigger. If null, an empty
     * {@link ActionSet} is returned instead.
     * @return The actions for this trigger (never null)
     */
    public ActionSet getActions() {
        if (actions == null) {
            return new ActionSet();
        } else {
            ActionSet result = new ActionSet();
            result.setActions(actions);
            return result;
        }
    }

}
