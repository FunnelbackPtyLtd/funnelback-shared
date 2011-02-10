package com.funnelback.publicui.search.lifecycle.input.processors;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Sets the "rmcf" query processor option on the fly if faceted navigation
 * is enabled on this collection.
 */
@Log
@Component("facetedNavigationInputProcessor")
public class FacetedNavigation implements InputProcessor {

	@Override
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) {
		if (SearchTransactionUtils.hasCollection(searchTransaction)) {
			
			// Default config from the live directory
			FacetedNavigationConfig config = searchTransaction.getQuestion().getCollection().getFacetedNavigationLiveConfig();

			// ...possibly overriden in collection config
			String configLocationOverride = searchTransaction.getQuestion().getCollection().getConfiguration().value(
					Keys.FacetedNavigation.CONFIG_LOCATION, DefaultValues.VIEW_LIVE);
			if (DefaultValues.FOLDER_CONF.equals(configLocationOverride)) {
				config = searchTransaction.getQuestion().getCollection().getFacetedNavigationConfConfig();
			}
			
			// If we have no config at this point, we can look at profiles
			if (config == null && searchTransaction.getQuestion().getProfile() != null) {
				Profile p = searchTransaction.getQuestion().getCollection().getProfiles().get(searchTransaction.getQuestion().getProfile());
				if (p != null) {
					// ...and at conf or live config depending of the override setting
					if (DefaultValues.FOLDER_CONF.equals(configLocationOverride)) {
						config = p.getFacetedNavConfConfig();
					} else {
						config = p.getFacetedNavLiveConfig();
					}
				}
			}
			
			if (config != null && config.getQpOptions() != null) {
				// Query Processor options are embedded in the faceted_navigation.cfg file:
				//
				// <Facets qpoptions=" -rmcfdt">
				//  <Data></Data>
				//  <Facet>
				//  ...
				//  </Facet>
				// </Facets>
				searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(config.getQpOptions());
				log.debug("Setting dynamic query processor option '" + config.getQpOptions() + "'");
			}
		}

	}

}
