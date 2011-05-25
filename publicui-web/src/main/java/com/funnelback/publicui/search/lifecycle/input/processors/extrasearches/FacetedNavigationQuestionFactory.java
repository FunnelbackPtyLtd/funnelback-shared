package com.funnelback.publicui.search.lifecycle.input.processors.extrasearches;

import java.util.Map;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;

public class FacetedNavigationQuestionFactory implements ExtraSearchQuestionFactory {

	private static final String NUM_RANKS_OPT = "-num_ranks0";
	
	@Override
	public SearchQuestion buildQuestion(SearchQuestion originalQuestion, Map<String, String> extraSearchConfiguration)
			throws InputProcessorException {
		SearchQuestion out = new SearchQuestion();
		SearchQuestionBinder.bind(originalQuestion, out);
		
		out.setFacetsGScopeConstraints(null);
		out.getFacetsQueryConstraints().clear();
		out.getDynamicQueryProcessorOptions().add(NUM_RANKS_OPT);
		
		return out;
	}

}
