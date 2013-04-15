package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>Pass user keys based on a "userkeys" request parameter, which would normally be added
 * by some external system which is wrapping the search results (a portal).</p>
 * 
 * <p>Note that this approach is not secure unless Funnelback can be accessed only via
 * the portal.</p>
 */
public class ManifoldCFMapper implements UserKeysMapper {

    /** Name of the URL parameter to use to pass the username 
     * (TODO - Remove this and use the remote user, it's just for testing
     * right now.) */
    public static final String USERNAME_PARAMETER_NAME = "user";
    
    @Override
    public List<String> getUserKeys(Collection currentCollection, SearchTransaction transaction) {
        try {
            String username = transaction.getQuestion().getInputParameterMap().get(USERNAME_PARAMETER_NAME);
            // TODO - Get the remote user instead somehow
            
            // TODO - Respect the currentCollection
            String authority = transaction.getQuestion().getCollection().getConfiguration().value(Keys.ManifoldCF.AUTHORITY_URL_PREFIX);
            String domain = transaction.getQuestion().getCollection().getConfiguration().value(Keys.ManifoldCF.AUTHORITY_URL_PREFIX);
            InputStream in = new URL(authority + "/UserACLs?username=" + username + "@" + domain).openStream();
            
            String authorityInfo;
            try {
                authorityInfo = IOUtils.toString(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
            
            // TODO - remove after debugging
            System.err.println(authorityInfo);
            
            List<String> result = new ArrayList<String>();
            if (authorityInfo != null) {
                for (String key: authorityInfo.split("\n")) {
                    result.addAll(Arrays.asList(key.split(",")));
                }
            }
            
            return result;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
