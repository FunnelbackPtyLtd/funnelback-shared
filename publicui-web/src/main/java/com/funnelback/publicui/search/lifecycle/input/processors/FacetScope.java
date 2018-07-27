package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.MapUtils;
import com.funnelback.publicui.utils.QueryStringUtils;
import com.google.common.collect.ObjectArrays;

import lombok.extern.log4j.Log4j2;

/**
 * Will convert a "facetScope" parameter (Set when clicking the
 * "within the same category" checkox) to valid facet parameters
 */
@Component("facetScopeInputProcessor")
@Log4j2
public class FacetScope extends AbstractInputProcessor {

    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
            if(searchTransaction.getQuestion().getRawInputParameters().get(RequestParameters.FACET_SCOPE) != null) {

                String facetScope = MapUtils.getFirstString(searchTransaction.getQuestion().getRawInputParameters(),
                        RequestParameters.FACET_SCOPE, null);
                
                if (facetScope != null && ! "".equals(facetScope)) {
                    Map<String, String[]> params = convertFacetScopeToParameters(facetScope);
                    params.entrySet()
                        .stream()
                        .map(e -> Pair.of(
                            e.getKey(), 
                            concatArray(e.getValue(), searchTransaction.getQuestion().getRawInputParameters().get(e.getKey()))))
                        .forEach(e -> searchTransaction.getQuestion().getRawInputParameters().put(e.getKey(), e.getValue()));
                    
                    if(log.isDebugEnabled()) {
                        log.debug("Transformed facetScope '" + facetScope 
                                + "' to question parameters '" + QueryStringUtils.arrayMapAsString(params) + "'");
                    }
                }
            }
        }
    }
    
    public String[] concatArray(String[] a1, String[] a2) {
        return ObjectArrays.concat(Optional.ofNullable(a1).orElse(new String[0]), 
            Optional.ofNullable(a2).orElse(new String[0]), String.class);
    }
    
    /**
     * Converts a <code>facetScope</code> query string parameters into
     * actual query strings parameters.
     * @param facetScope Query string value for the <code>facetScope</code> parameter
     * @return Query string parameters
     */
    public static Map<String, String[]> convertFacetScopeToParameters(String facetScope) {
        return QueryStringUtils.toArrayMap(facetScope);
    }
    
    /**
     * Converts the <code>facetScope</code> parameter from a query string to actual facets parameters
     * @param parameters Query string to convert
     * @return Converted URL. Will be identical to the input one if it didn't contain a <code>facetScope</code>
     */
    
    public static String convertFacetScopeParameters(String parameters) {
        
        Map<String, List<String>> qs = QueryStringUtils.toMap(parameters);
        if (qs.containsKey(RequestParameters.FACET_SCOPE)
            && qs.get(RequestParameters.FACET_SCOPE).size() > 0) {
            Map<String, String[]> params = convertFacetScopeToParameters(qs.get(RequestParameters.FACET_SCOPE).get(0));
            qs.remove(RequestParameters.FACET_SCOPE);
            qs.putAll(MapUtils.convertMapList(params));
        
            return QueryStringUtils.toString(qs, false);
        } else {
            return parameters;
        }
    }
}
