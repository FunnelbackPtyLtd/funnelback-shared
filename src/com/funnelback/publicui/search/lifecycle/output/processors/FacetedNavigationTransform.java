package com.funnelback.publicui.search.lifecycle.output.processors;

import groovy.lang.Binding;
import groovy.lang.Script;
import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetedNavigationUtils;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Executes a faceted navigation transform script.
 */
@Component("facetedNavigationTransformOutputProcessor")
@Log
public class FacetedNavigationTransform implements OutputProcessor{

	private static final String KEY_FACETS = "facets";
	
	@Override
	public void process(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (searchTransaction != null
				&& SearchTransactionUtils.hasCollection(searchTransaction)
				&& searchTransaction.hasResponse()
				&& searchTransaction.getResponse().getFacets().size() > 0) {
			
			FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
			
			if (config != null && config.getTransformScript() != null) {
				Script s = config.getTransformScript();
				Binding binding = new Binding();
				binding.setVariable(KEY_FACETS, searchTransaction.getResponse().getFacets());
				s.setBinding(binding);
				try {
					s.run();
				} catch (Throwable t) {
					log.error("Error while executing faceted navigation transform script", t);
				}
			}
		}
	}

}
