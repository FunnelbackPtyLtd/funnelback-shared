package com.funnelback.publicui.search.model.curator.config;

import java.util.HashMap;
import java.util.Map;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CuratorConfig represents the configuration of the curator system which can be
 * used to modify result presentation under certain conditions.
 */
@AllArgsConstructor
@NoArgsConstructor
public class CuratorConfig {

    /**
     * A map describing the sets of actions to be performed when a request
     * matching the given trigger is performed.
     */
    @Getter
    @Setter
    private Map<Trigger, ActionSet> triggerActions = new HashMap<Trigger, ActionSet>();

    /**
     * <p>
     * Determines, based on the given searchTransaction, whether the curator
     * system should be activated. If it is not activated the curator system
     * will not modify the standard search response, so disabling it may be
     * useful for debugging purposes.
     * </p>
     * 
     * <p>
     * The current implementation of this check defaults to active, but examines
     * the CGI parameter 'curator' and will disable the curator system if it has
     * any value other than 'true' (case insensitive).
     * </p>
     */
    public static boolean isCuratorActive(SearchTransaction searchTransaction) {
        return (!searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.CURATOR))
            || Boolean.parseBoolean(searchTransaction.getQuestion().getInputParameterMap()
                .get(RequestParameters.CURATOR));
    }
}
