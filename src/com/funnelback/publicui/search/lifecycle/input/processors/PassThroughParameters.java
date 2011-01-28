package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Will collect all the parameters of the {@link HttpServletRequest} parameters and
 * populate the "passThroughParameters" Set of the {@link SearchQuestion}. These
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
public class PassThroughParameters implements InputProcessor {

	/**
	 * Some parameters are ignored as we deal with them specifically
	 */
	public static final String[] IGNORED = {RequestParameters.QUERY, RequestParameters.COLLECTION};
	
	@SuppressWarnings("unchecked")
	@Override
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) {
		if (searchTransaction != null
				&& searchTransaction.getQuestion() != null
				&& request != null) {
			searchTransaction.getQuestion().getPassThroughParameters().putAll(new HashMap<String, String[]>(request.getParameterMap()));
		
			for (String ignored: IGNORED) {
				searchTransaction.getQuestion().getPassThroughParameters().remove(ignored);
			}
		}
	}
}
