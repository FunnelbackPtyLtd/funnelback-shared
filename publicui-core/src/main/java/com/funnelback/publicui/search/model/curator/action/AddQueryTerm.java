package com.funnelback.publicui.search.model.curator.action;

import lombok.EqualsAndHashCode;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>Action to add a specified term to the users query before running it.</p>
 * 
 * <p>The term will be added to the 's' parameter, not the 'query' one, meaning that
 * the original user's query will be logged without the additional term.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class AddQueryTerm implements Action {

    /**
     * The term to add to the user's query.
     */
    @Getter
    @Setter
    private String queryTerm;

    /**
     * Add the specified query term to the user's query via the 's' parameter.
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        Map<String, String> inputParameterMap = searchTransaction.getQuestion().getInputParameterMap();
        
        if (inputParameterMap.get(RequestParameters.S) == null) {
            inputParameterMap.put(RequestParameters.S, queryTerm);
        } else {
            inputParameterMap.put(RequestParameters.S, inputParameterMap.get(RequestParameters.S) + " " + queryTerm);
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
