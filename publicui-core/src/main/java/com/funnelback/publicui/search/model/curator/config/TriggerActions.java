package com.funnelback.publicui.search.model.curator.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
     * Name for the TriggerAction (a nice name to present to the user)
     */
    @Getter
    @Setter
    private String name;    

    /**
     * Id for the TriggerAction (a unique ID to refer to the specific item by)
     */
    @Getter
    @Setter
    private String Id = null;    

    /**
     * Indicates whether this TriggerActions is enabled (i.e. should be considered for new requests).
     */
    @Getter
    @Setter
    private Boolean enabled = true;    

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
    private List<Action> actions = new ArrayList<Action>();

    /**
     * Get the set of actions for this trigger. If null, an empty
     * {@link ActionSet} is returned instead.
     * @return The actions for this trigger (never null)
     */
    @JsonIgnore
    public ActionSet getActionSet() {
        if (actions == null) {
            return new ActionSet();
        } else {
            ActionSet result = new ActionSet();
            result.setActions(actions);
            return result;
        }
    }

    /** Configure this object and Triggers/Actions below */
    public void configure(Configurer configurer) {
        configurer.configure(this);
        getActionSet().configure(configurer);
        trigger.configure(configurer);
    }

}
