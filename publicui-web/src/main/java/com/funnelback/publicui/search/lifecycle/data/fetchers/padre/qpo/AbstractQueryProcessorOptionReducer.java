package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Abstract {@link QueryProcessorOptionReducer} which can be configured with a list of QP option
 * names it supports
 * 
 * @author nguillaumin@funnelback.com
 *
 * @param <T> Type of the values to reduce
 */
public abstract class AbstractQueryProcessorOptionReducer<T> implements QueryProcessorOptionReducer<T> {

    /** Option names supported by this reducer */
    private final List<String> supportedOptions;

    /**
     * @param supportedOptions Option names supported by this reducer
     */
    public AbstractQueryProcessorOptionReducer(String... supportedOptions) {
        this.supportedOptions = Arrays.asList(supportedOptions);
    }

    @Override
    public Pair<String, String> reduce(String queryProcessorOptionName, List<T> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }

        return reduceInternal(queryProcessorOptionName, values);
    }

    /**
     * Actual reduction method to be implemented by subclasses
     * 
     * @param queryProcessorOptionName name of the query processor option
     * @param values QP option values. Will never be empty as a QPO must have at
     * least one value
     * @return Pair of (option name, single value)
     */
    abstract protected Pair<String, String> reduceInternal(String queryProcessorOptionName, List<T> values);

    @Override
    public boolean supports(String queryProcessorOptionName) {
        return supportedOptions.contains(queryProcessorOptionName);
    }

}
