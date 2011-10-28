package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Pass user keys based on a "userkeys" request parameter, which would normally be added
 * by some external system which is wrapping the search results (a portal).
 * 
 * Note that this approach is not secure unless Funnelback can be accessed only via
 * the portal.
 */
public class PortalKeyMapper implements UserKeysMapper {

	public static final String PORTAL_PARAMETER_NAME = "userkeys";
	
	@Override
	public List<String> getUserKeys(SearchTransaction transaction) {
		String userKeys = transaction.getQuestion().getAdditionalParameters().get(PORTAL_PARAMETER_NAME);
		List<String> result = new ArrayList<String>();
		if (userKeys != null) {
			result = Arrays.asList(userKeys.split(","));
		}
		return result;
	}

}
