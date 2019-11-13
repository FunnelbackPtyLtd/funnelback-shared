package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.util.HashMap;
import java.util.Map;

import com.funnelback.config.keys.Keys;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class CfgOptionsPassedByEnvironment {

    /**
     * Defines collection.cfg (and friends) options which are to be passed in to
     * padre using the environment.
     * 
     * Some options may come from profile.cfg, such as the only currently supported option of
     * "query_processor_options"
     * 
     * Note that this is passed in using the environment which is also in the search transaction, however
     * we do not want to support allowing users to set QPOs into the environment which are also set here.
     * Either ensure that all collection.cfg/profile.cfg that are read over an environment are set
     * in the return mapped OR come up with some way to have those setting removed/ignored from
     * {@link SearchQuestion#getEnvironmentVariables()}.
     * 
     * @return A map defining collection.cfg and profile.cfg settings that padre should use
     * instead of reading from those places in padre.
     */
    public Map<String, String> cfgOptoons(SearchTransaction st) {
        Map<String, String> map = new HashMap<>();
        map.put(Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS.getKey(), 
            st.getQuestion().getCurrentProfileConfig().get(Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS));
        return map;
    }
}
