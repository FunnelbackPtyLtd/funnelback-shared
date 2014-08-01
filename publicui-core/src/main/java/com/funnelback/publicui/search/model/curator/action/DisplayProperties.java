package com.funnelback.publicui.search.model.curator.action;

import org.codehaus.jackson.annotate.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.data.Properties;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Action to display the specified properties within the search results.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DisplayProperties implements Action {

    /** The properties object to be displayed. */
    @Getter
    @Setter
    @JsonUnwrapped
    private Properties additionalProperties;

    /** Adds the action's properties object to the curator model's exhibits. */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        searchTransaction.getResponse().getCurator().getExhibits().add(additionalProperties);
    }

    /**
     * Properties are added only in the output phase (once the response object
     * has been created from Padre's output).
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return phase.equals(Phase.OUTPUT);
    }
    
    /** Configure this action (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
