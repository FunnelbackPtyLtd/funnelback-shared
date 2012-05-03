package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Map;

import lombok.extern.log4j.Log4j;

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
@Log4j
public class FacetScope implements InputProcessor {

	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
			if(searchTransaction.getQuestion().getRawInputParameters().get(RequestParameters.FACET_SCOPE) != null) {

				String facetScope = MapUtils.getFirstString(searchTransaction.getQuestion().getRawInputParameters(),
						RequestParameters.FACET_SCOPE, null);
				
				if (facetScope != null && ! "".equals(facetScope)) {
					Map<String, String> qs = QueryStringUtils.toSingleMap(facetScope, false);
					searchTransaction.getQuestion().getRawInputParameters().putAll(QueryStringUtils.toArrayMap(facetScope, false));
					log.debug("Transformed facetScope '" + facetScope + "' to question parameters '" + qs + "'");
				}
			}
		}
	}
}
