package com.funnelback.publicui.search.model.curator.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CuratorYamlConfig represents the configuration of the curator system which can be
 * used to modify result presentation under certain conditions as it was represented
 * in yaml in v13.0.0.
 *
 * This format was changed to JSON (and changed to a list rather than a map with
 * non-string keys) in v13.4 to avoid problems in the JSON representation.
 * 
 * @since 13.4
 */
@AllArgsConstructor
@NoArgsConstructor
public class CuratorYamlConfig {

    /**
     * <p>The map of triggers and associated actions for this curator configuration.</p>
     * 
     * <p>The actions will be performed when a request matching the given trigger is detected.</p>
     * 
     * @since 13.4
     */
    @Getter
    @Setter
    private Map<Trigger, ActionSet> triggerActions = new HashMap<Trigger, ActionSet>();
    
    /**
     * <p>Convert the internal map into a TriggerAction list compatible with the new format.</p>
     *
     * @since 14.0
     */
    public List<TriggerActions> toTriggerActions() {
        List<TriggerActions> result = new ArrayList<TriggerActions>();
        
        for (Trigger t : triggerActions.keySet()) {
            TriggerActions item = new TriggerActions();
            item.setTrigger(t);
            item.setActions(triggerActions.get(t));
            result.add(item);
        }
        
        return result;
    }
}
