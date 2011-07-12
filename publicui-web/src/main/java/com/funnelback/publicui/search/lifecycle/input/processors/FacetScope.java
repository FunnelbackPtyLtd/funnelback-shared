package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.MapUtils;
import com.funnelback.publicui.utils.QueryStringUtils;

/**
 * Will convert a "facetScope" parameter (Set when clicking the
 * "within the same category" checkox) to valid facet parameters
 */
@Component("facetScopeInputProcessor")
@Log
public class FacetScope implements InputProcessor {

	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
			if(searchTransaction.getQuestion().getAdditionalParameters().get(RequestParameters.FACET_SCOPE) != null) {

				String facetScope = MapUtils.getString(searchTransaction.getQuestion().getInputParameterMap(),
						RequestParameters.FACET_SCOPE, null);
				
				if (facetScope != null && ! "".equals(facetScope)) {
					Map<String, String> qs = QueryStringUtils.toSingleMap(facetScope, false);
					searchTransaction.getQuestion().getInputParameterMap().putAll(qs);
					searchTransaction.getQuestion().getRawInputParameters().putAll(QueryStringUtils.toArrayMap(facetScope, false));
					log.debug("Transformed facetScope '" + facetScope + "' to question parameters '" + qs + "'");
				}
			}
		}
	}
}
