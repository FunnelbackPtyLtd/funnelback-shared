package com.funnelback.publicui.search.model.curator.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>Action to set the query to the given value, replacing any previously specified by the user.</p>
 * 
 * <p>Optionally, may also clear any metadata or special query parameters (e.g. query_and) as well.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SetQuery implements Action {

    /** The new query to be used. */
    @Getter @Setter private String query;

    /**
     * Whether to remove all other query parameters (such as meta_x and
     * query_or), in addition to the simple query one.
     */
    @Getter @Setter private Boolean clearAllQueryParameters = false;

    /**
     * Replace occurrences of target with replacement within the question's query and metaParameters.
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        searchTransaction.getQuestion().setQuery(query);
        
        if (clearAllQueryParameters) {
            searchTransaction.getQuestion().getMetaParameters().clear();
        }
    }

    /**
     * Query term addition occurs in the INPUT phase since it must modify
     * the query before it is run by padre.
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return phase.equals(Phase.INPUT);
    }

    /** Configure this action (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
