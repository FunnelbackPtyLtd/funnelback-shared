package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Gets keys from ManifoldCF based on the REMOTE_USER as defined by the web
 * server. This would normally be used with the Modern UI's authentication
 * support on Windows to authenticate users against Active Directory.
 */
public class ManifoldCFMapper extends AbstractManifoldCFMapper {

    public ManifoldCFMapper() throws NoSuchAlgorithmException, KeyManagementException {
        super();
    }

    /**
     * Gets the username from REMOTE_USER and appends '@' and the domain to it.
     * 
     * Currently we strip the domain which comes with the user on Windows and replace it
     * with the one decalared in the collection.cfg file because we get HARNESS in the user
     * but ManifoldCF expects harness.local
     * 
     * I'm not sure if there is some more principled way to do this, but if there is we should
     * do that instead.
     */
    public String getFullUsername(SearchTransaction transaction) {
        String remoteUser = transaction.getQuestion().getInputParameterMap().get("REMOTE_USER");
        
        String[] domainAndUser = remoteUser.split("\\\\");
        if (domainAndUser.length != 2) {
            throw new RuntimeException("Remote username (" + remoteUser
                + ") not in the expected format (Expected one slash separating domain and username).");
        }
        @SuppressWarnings("unused")
        String domain = domainAndUser[0];
        String user = domainAndUser[1];
        
        String configDomain = transaction.getQuestion().getCollection().getConfiguration().value(Keys.ManifoldCF.DOMAIN);
        
        return user + "@" + configDomain;
    }
    
}
