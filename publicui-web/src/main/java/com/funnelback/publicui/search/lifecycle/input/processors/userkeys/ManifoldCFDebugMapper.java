package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Gets keys from a ManifoldCF authority based on a CGI user parameter
 * which allows us to easily debug the system.
 * 
 * Note - This is obviously totally insecure (anyone can edit the parameter value)
 * so this mapper should not be used in production unless users are prevented from
 * directly accessing Funnelback somehow.
 */
public class ManifoldCFDebugMapper extends AbstractManifoldCFMapper {

    /** 
     * Name of the URL parameter to use to pass the username
     */
    public static final String USERNAME_PARAMETER_NAME = "user";

    public ManifoldCFDebugMapper() throws NoSuchAlgorithmException, KeyManagementException {
        super();
    }

    /**
     * Get the username based on a CGI parameter and a domain defined in the collection.cfg
     */
    protected String getFullUsername(SearchTransaction transaction) {
        String username = transaction.getQuestion().getInputParameterMap().get(USERNAME_PARAMETER_NAME);
        
        String domain = transaction.getQuestion().getCollection().getConfiguration().value(Keys.ManifoldCF.DOMAIN);
        
        String fullUsername = username + "@" + domain;
        return fullUsername;
    }
    
}
