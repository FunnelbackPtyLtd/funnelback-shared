package com.funnelback.publicui.search.lifecycle.input.processors.extrasearches;

import java.util.Map;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.utils.MapKeyFilter;

public class FacetedNavigationQuestionFactory implements ExtraSearchQuestionFactory {

	private static final String NUM_RANKS_OPT = "-numranks0";
	
	@Override
	public SearchQuestion buildQuestion(SearchQuestion originalQuestion, Map<String, String> extraSearchConfiguration)
			throws InputProcessorException {
		SearchQuestion out = new SearchQuestion();
		SearchQuestionBinder.bind(originalQuestion, out);
		
		MapKeyFilter filter = new MapKeyFilter(originalQuestion.getInputParameterMap());
		String[] selectedFacetsParams = filter.filter(RequestParameters.FACET_PARAM_PATTERN);
		for (String paramName: selectedFacetsParams) {
			out.getInputParameterMap().remove(paramName);
		}
		
		out.setFacetsGScopeConstraints(null);
		out.getFacetsQueryConstraints().clear();
		out.getDynamicQueryProcessorOptions().add(NUM_RANKS_OPT);
		
		return out;
	}

}
