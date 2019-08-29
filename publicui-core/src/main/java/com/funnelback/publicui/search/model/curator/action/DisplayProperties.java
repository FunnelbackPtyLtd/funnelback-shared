package com.funnelback.publicui.search.model.curator.action;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.funnelback.publicui.search.model.curator.HasNoBeans;
import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.data.Properties;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Action to display the specified properties within the search results.
 */
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public final class DisplayProperties implements Action, HasNoBeans {

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
