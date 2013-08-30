package com.funnelback.publicui.curator.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.data.Properties;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Action to display the specified properties within the search results.
 */
@AllArgsConstructor
@NoArgsConstructor
public class DisplayProperties implements Action {

    /** The properties object to be displayed. */
    @Getter
    @Setter
    private Properties properties;

    /** Adds the action's properties object to the curator model's exhibits. */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        searchTransaction.getResponse().getCurator().getExhibits().add(properties);
    }

    /**
     * Properties are added only in the output phase (once the response object
     * has been created from Padre's output).
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return phase.equals(Phase.OUTPUT);
    }
}
