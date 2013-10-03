package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Will collect all the parameters of the {@link HttpServletRequest} parameters and
 * populate the "additionalParameters" field of the {@link SearchQuestion}. These
 * parameters will be passed to PADRE.
 * 
 * Should be executed first since other {@link InputProcessor} are likely
 * to update this Set to remove parameters they've processed.
 * 
 * Some parameters are ignored such as 'query' and 'collection' as we deal with them
 * specifically.
 *
 */
@Component("passThroughParametersInputProcessor")
public class PassThroughParameters extends AbstractInputProcessor {

    /**
     * Names of the parameters to ignore (Irrelevant to PADRE, or because
     * we deal with them specifically)
     */
    public static final String[] IGNORED_NAMES = {
        RequestParameters.QUERY, RequestParameters.COLLECTION, RequestParameters.PROFILE,
        RequestParameters.CLIVE, RequestParameters.ContextualNavigation.CN_CLICKED,
        RequestParameters.ONESHOT, // FUN-4328: Ignore oneshot parameter
        RequestParameters.ENC,      // Input parameters are always UTF-8
        RequestParameters.S
    };
    
    /**
     * Pattern of parameter names to ignore
     */
    public static final Pattern[] IGNORED_PATTERNS = {
        RequestParameters.ContextualNavigation.CN_PREV_PATTERN
    };
    
    @Override
    public void processInput(SearchTransaction searchTransaction) {
        if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
            searchTransaction.getQuestion().getAdditionalParameters().putAll(searchTransaction.getQuestion().getRawInputParameters());
        
            for (String ignored: IGNORED_NAMES) {
                searchTransaction.getQuestion().getAdditionalParameters().remove(ignored);
            }
            
            for (Pattern ignored: IGNORED_PATTERNS) {
                for (String paramName: searchTransaction.getQuestion().getAdditionalParameters().keySet().toArray(new String[0])) {
                    if (ignored.matcher(paramName).matches()) {
                        searchTransaction.getQuestion().getAdditionalParameters().remove(paramName);
                    }
                }
            }
        }
    }
}
