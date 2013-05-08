package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class ManifoldCFMapper extends AbstractManifoldCFMapper {

    public ManifoldCFMapper() throws NoSuchAlgorithmException, KeyManagementException {
        super();
    }

    protected String getFullUsername(SearchTransaction transaction) {
        // TODO - Needs to be reformatted to the format ManifoldCF expects.
        
        return transaction.getQuestion().getInputParameterMap().get("REMOTE_USER");
    }
    
}
