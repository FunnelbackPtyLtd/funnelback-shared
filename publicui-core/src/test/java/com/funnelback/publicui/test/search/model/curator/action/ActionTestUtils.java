package com.funnelback.publicui.test.search.model.curator.action;

import org.springframework.context.ApplicationContext;

import com.funnelback.publicui.search.model.curator.Curator;
import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class ActionTestUtils {

    public static SearchTransaction runAllPhases(Action action) {
        SearchResponse response = new SearchResponse();
        response.setCurator(new Curator());
        SearchTransaction searchTransaction = new SearchTransaction(new SearchQuestion(), response);
        return runAllPhases(action, searchTransaction, null);
    }
    
    public static SearchTransaction runAllPhases(Action action, SearchTransaction searchTransaction, ApplicationContext context) {
        for (Action.Phase phase : Action.Phase.values()) {
            if (action.runsInPhase(phase)) {
                action.performAction(searchTransaction, phase);
            }
        }
        
        return searchTransaction;
    }
    
}