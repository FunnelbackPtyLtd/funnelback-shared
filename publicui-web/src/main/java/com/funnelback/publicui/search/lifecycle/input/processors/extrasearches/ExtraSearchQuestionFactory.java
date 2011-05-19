package com.funnelback.publicui.search.lifecycle.input.processors.extrasearches;

import java.util.Map;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;

/**
 * <p>Part of the extra search feature: builds a new {@link SearchQuestion},
 * given an existing one and a configuration.</p>
 * 
 * <p>Implementation should define their own rules on how to clone the original
 * {@link SearchQuestion} to generate a new one. A basic implementation could
 * for instance just change the target collection, in order to perform an extra
 * search identical of the original one but on a different collection.</p>
 *
 */
public interface ExtraSearchQuestionFactory {

	/**
	 * Builds a question for this specific extra search, given an original question
	 * and a set of parameters.
	 * @param originalQuestion
	 * @param extraSearchConfiguration
	 * @return
	 */
	public SearchQuestion buildQuestion(SearchQuestion originalQuestion, Map<String, String> extraSearchConfiguration) throws InputProcessorException;
	
}
