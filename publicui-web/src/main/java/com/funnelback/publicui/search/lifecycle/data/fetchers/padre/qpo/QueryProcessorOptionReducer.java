package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Reduces multiple query processor options in a single one suitable to be passed to PADRE.
 * 
 * @author nguillaumin@funnelback.com
 *
 * @param <T> Type of the value to reduce
 */
public interface QueryProcessorOptionReducer<T> {

    /**
     * Reduce a query processor option with multiple values into a single one
     * 
     * @param queryProcessorOptionName name of the query processor option
     * @param values QP option values. Will never be empty as a QPO must have at
     * least one value
     * @return Pair of (option name, single value)
     */
    public Pair<String, String> reduce(String queryProcessorOptionName, List<T> values);

    /**
     * @param queryProcessorOptionName
     * @return true if the reducer supports reducing this option, false otherwise
     */
    public boolean supports(String queryProcessorOptionName);

}
