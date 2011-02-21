package com.funnelback.publicui.search.model.collection.facetednavigation;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;

public class FacetedNavigationUtils {

	/**
	 * Will select the correct faceted navigation configuration depending on
	 * collection parameters (Is there a config location override ?) and the presence
	 * of a profile.
	 * @param c
	 * @param p
	 * @return
	 */
	public static FacetedNavigationConfig selectConfiguration(Collection c, String profileId) {
		// Default config from the live directory
		FacetedNavigationConfig config = c.getFacetedNavigationLiveConfig();

		// ...possibly overriden in collection config
		String configLocationOverride = c.getConfiguration().value(Keys.FacetedNavigation.CONFIG_LOCATION, DefaultValues.VIEW_LIVE);
		if (DefaultValues.FOLDER_CONF.equals(configLocationOverride)) {
			config = c.getFacetedNavigationConfConfig();
		}
		
		// If we have no config at this point, we can look at profiles
		if (config == null && profileId != null) {
			Profile p = c.getProfiles().get(profileId);
			if (p != null) {
				// ...and at conf or live config depending of the override setting
				if (DefaultValues.FOLDER_CONF.equals(configLocationOverride)) {
					config = p.getFacetedNavConfConfig();
				} else {
					config = p.getFacetedNavLiveConfig();
				}
			}
		}
		
		return config;
	}
	
}
