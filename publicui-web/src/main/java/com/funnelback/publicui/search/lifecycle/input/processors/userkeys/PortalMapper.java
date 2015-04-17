package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.log4j.Log4j2;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>Pass user keys based on a "userkeys" request parameter, which would normally be added
 * by some external system which is wrapping the search results (a portal).</p>
 * 
 * <p>Note that this approach is not secure unless Funnelback can be accessed only via
 * the portal.</p>
 */
@Log4j2
public class PortalMapper implements UserKeysMapper {

    /** Name of the URL parameter to use to pass the keys */
    public static final String PORTAL_PARAMETER_NAME = "userkeys";
    
    @Override
    public List<String> getUserKeys(Collection collection, SearchTransaction transaction) {
        String[] userKeys = transaction.getQuestion().getRawInputParameters().get(PORTAL_PARAMETER_NAME);
        List<String> result = new ArrayList<String>();
        if (userKeys != null) {
            for (String key: userKeys) {
                result.addAll(Arrays.asList(key.split(",")));
            }
            log.debug("Set userkeys for request to " + result);
        } else {
            log.debug("No userkeys set for request");            
        }
        return result;
    }

}
