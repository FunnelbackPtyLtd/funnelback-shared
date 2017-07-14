package com.funnelback.publicui.search.lifecycle.input.processors.extrasearches;

import java.util.Map;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.utils.MapKeyFilter;

public class FacetedNavigationQuestionFactory implements ExtraSearchQuestionFactory {

    private static final String NUM_RANKS_OPT = "-num_ranks=1";
    
    @Override
    public SearchQuestion buildQuestion(SearchQuestion originalQuestion, Map<String, String> extraSearchConfiguration)
        throws InputProcessorException {
        SearchQuestion out = buildBasicExtraFacetSearch(originalQuestion);
        
        // Remove faceted navigation parameters
        MapKeyFilter filter = new MapKeyFilter(originalQuestion.getRawInputParameters());
        String[] selectedFacetsParams = filter.filter(RequestParameters.FACET_PARAM_PATTERN);
        for (String paramName: selectedFacetsParams) {
            out.getRawInputParameters().remove(paramName);
        }
        
        // Remove additional 'facetScope' parameter used in faceted navigation
        out.getRawInputParameters().remove(RequestParameters.FACET_SCOPE);
        
        out.setFacetsGScopeConstraints(null);
        out.getFacetsQueryConstraints().clear();
        out.getDynamicQueryProcessorOptions().add(NUM_RANKS_OPT);
        
        return out;
    }
    
    public SearchQuestion buildBasicExtraFacetSearch(SearchQuestion originalQuestion) {
        SearchQuestion out = new SearchQuestion();
        SearchQuestionBinder.bind(originalQuestion, out);
        return out;
    }
    

}
