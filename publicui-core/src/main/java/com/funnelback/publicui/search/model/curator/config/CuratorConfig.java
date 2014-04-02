package com.funnelback.publicui.search.model.curator.config;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * CuratorConfig represents the configuration of the curator system which can be
 * used to modify result presentation under certain conditions.
 * 
 * @since 13.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class CuratorConfig {

    /**
     * <p>The list of triggers and associated actions for this curator configuration.</p>
     * 
     * <p>The actions will be performed when a request matching the given trigger is detected.</p>
     * 
     * @since 13.4
     */
    @Getter
    @Setter
    private List<TriggerActions> triggerActions = new ArrayList<TriggerActions>();
    
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
     * the CGI parameter <code>curator</code> and will disable the curator system if it has
     * any value other than 'true' (case insensitive).
     * </p>
     * 
     * @param searchTransaction Current search transaction
     * @return true if the curator is active, false otherwise
     */
    public static boolean isCuratorActive(SearchTransaction searchTransaction) {
        return (!searchTransaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.CURATOR))
            || Boolean.parseBoolean(searchTransaction.getQuestion().getInputParameterMap()
                .get(RequestParameters.CURATOR));
    }
}
