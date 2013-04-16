package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
            String domain = transaction.getQuestion().getCollection().getConfiguration().value(Keys.ManifoldCF.DOMAIN);
            InputStream in = new URL(authority + "/UserACLs?username=" + username + "@" + domain).openStream();
            
            String authorityInfo;
            try {
                authorityInfo = IOUtils.toString(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
            
            // TODO - remove after debugging
            System.err.println("Authority output\n\n" + authorityInfo);
            List<String> result = getKeysFromAuthorityInfo(authorityInfo);
            
            return result;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getKeysFromAuthorityInfo(String authorityInfo) {
        List<String> result = new ArrayList<String>();
        if (authorityInfo != null) {
            for (String line: authorityInfo.split("\n")) {
                String[] fields = line.split(":");
                
                if (fields.length == 3 && "TOKEN".equals(fields[0])) {
                    result.add(fields[2]);
                } else if (fields.length == 2 && "AUTHORIZED".equals(fields[0])) {
                    // Not sure what to do with these
                } else {
                    // TODO - Work out what other cases are possible and handle them
                    throw new RuntimeException("Unrecognised authority output: " + line);
                }
            }
        }
        return result;
    }
}
