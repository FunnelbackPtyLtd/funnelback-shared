package com.funnelback.publicui.test.search.lifecycle.input.processors.extrasearches;

import java.util.Map;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.ExtraSearchQuestionFactory;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;

public class ChangeQueryQuestionFactory implements ExtraSearchQuestionFactory {

    @Override
    public SearchQuestion buildQuestion(SearchQuestion originalQuestion, Map<String, String> extraSearchConfiguration)
        throws InputProcessorException {
        SearchQuestion out = new SearchQuestion();
        SearchQuestionBinder.bind(originalQuestion, out);

        out.setQuery("HAS BEEN CHANGED");
        
        return out;
    }

}
