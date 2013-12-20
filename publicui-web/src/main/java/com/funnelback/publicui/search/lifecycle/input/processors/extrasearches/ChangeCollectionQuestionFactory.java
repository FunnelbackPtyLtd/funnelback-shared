package com.funnelback.publicui.search.lifecycle.input.processors.extrasearches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.Keys;
import com.funnelback.common.utils.CommandLineUtils;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;

/**
 * Implementation of an {@link ExtraSearchQuestionFactory}
 * that keep the query but changes the target collection given
 * the configuration.
 * 
 */
public class ChangeCollectionQuestionFactory implements ExtraSearchQuestionFactory {
    
    @Autowired
    private ConfigRepository configRepository;
    
    @Override
    public SearchQuestion buildQuestion(SearchQuestion originalQuestion, Map<String, String> extraSearchConfiguration) throws InputProcessorException {
        SearchQuestion out = new SearchQuestion();
        SearchQuestionBinder.bind(originalQuestion, out);
        
        String collectionId = extraSearchConfiguration.get(Keys.COLLECTION);
        if (collectionId != null) {
            Collection c = configRepository.getCollection(collectionId);
            if (c != null) {
                out.setCollection(c);
            } else {
                throw new InputProcessorException("Invalid collection parameter '" + collectionId + "'");
            }
        } else {
            throw new InputProcessorException("Collection parameter cannot be null");
        }
        
        if (extraSearchConfiguration.get(Keys.QUERY_PROCESSOR_OPTIONS) != null) {
            List<String> extraSearchOptions = 
                CommandLineUtils.splitCommandLineOptions(extraSearchConfiguration.get(Keys.QUERY_PROCESSOR_OPTIONS));
            out.getDynamicQueryProcessorOptions().addAll(extraSearchOptions);
        }
        
        return out;
    }

}
