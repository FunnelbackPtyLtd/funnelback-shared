package com.funnelback.publicui.search.lifecycle.input.processors;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetedNavigationUtils;
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
			
			FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
			
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
