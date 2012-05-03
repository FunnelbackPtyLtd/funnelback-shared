package com.funnelback.publicui.search.lifecycle.input.processors.extrasearches;

import java.util.Map;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;

public class ContentOptimiserSelectUrlQuestionFactory implements
		ExtraSearchQuestionFactory {

	@Override
	public SearchQuestion buildQuestion(SearchQuestion originalQuestion,
			Map<String, String> extraSearchConfiguration)
			throws InputProcessorException {
		SearchQuestion out = new SearchQuestion();
		SearchQuestionBinder.bind(originalQuestion, out);
		
		out.getRawInputParameters().put("xscope", new String[] {extraSearchConfiguration.get(RequestParameters.CONTENT_OPTIMISER_URL)});
//		out.getInputParameterMap().put("daat" , new String[] { "off"});
		return out;
	}

}
