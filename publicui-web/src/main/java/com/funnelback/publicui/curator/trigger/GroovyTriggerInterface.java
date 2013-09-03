package com.funnelback.publicui.curator.trigger;

import java.util.Map;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Interface representing a curator trigger which can be implemented in an
 * external Groovy class (to be subsequently used from within GroovyTrigger).
 */
public interface GroovyTriggerInterface {

    /**
     * @return Return true if the trigger should activate on this request, and
     *         false otherwise.
     */
    boolean activatesOn(SearchTransaction searchTransaction, Map<String, Object> properties);

}
