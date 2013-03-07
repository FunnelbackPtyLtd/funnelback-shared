package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.Arrays;
import java.util.List;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Always pass the master key
 */
public class MasterKeyMapper implements UserKeysMapper {

    public static final String MASTER_KEY = "master";
    
    @Override
    public List<String> getUserKeys(SearchTransaction transaction) {
        return Arrays.asList(new String[] {MASTER_KEY});
    }

}
