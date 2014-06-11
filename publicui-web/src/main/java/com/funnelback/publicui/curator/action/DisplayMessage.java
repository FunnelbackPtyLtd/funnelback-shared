package com.funnelback.publicui.curator.action;

import org.springframework.context.ApplicationContext;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.data.Message;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Action to display the specified message within the search results.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DisplayMessage implements Action {

    /** The message to display when this action is performed. */
    @Getter
    @Setter
    private Message message;

    /**
     * Add the given message to the curator model's exhibits in the given
     * searchTransaction.
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase, ApplicationContext context) {
        searchTransaction.getResponse().getCurator().getExhibits().add(message);
    }

    /**
     * Messages are added only in the output phase (once the response object has
     * been created from Padre's output).
     */
    @Override
    public boolean runsInPhase(Phase phase, ApplicationContext context) {
        return phase.equals(Phase.OUTPUT);
    }
}
