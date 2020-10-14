package com.funnelback.plugin.servlet.filter;

import com.funnelback.publicui.search.model.collection.ServiceConfig;

public interface SearchServletFilterHookContext {

    /**
     * Returns the (modern) config of the currentProfile for the search request.
     */
    ServiceConfig getCurrentProfileConfig();
}
