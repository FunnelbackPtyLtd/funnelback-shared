package com.funnelback.publicui.form.converter.operations;

import com.funnelback.publicui.form.converter.Operation;

public class SwitchToGlobalResources implements Operation {

    private static final String PREFIX_TO_REPLACE = "${SearchPrefix}";
    private static final String GLOBAL_RESOURCES_PREFIX = "${GlobalResourcesPrefix}";
    
    
    @Override
    public String process(String in) {
        return in.replace(PREFIX_TO_REPLACE, GLOBAL_RESOURCES_PREFIX);
    }

}
