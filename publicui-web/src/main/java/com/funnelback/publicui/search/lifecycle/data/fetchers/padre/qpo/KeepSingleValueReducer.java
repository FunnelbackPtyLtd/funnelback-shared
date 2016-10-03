package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo;

import java.util.List;
import java.util.function.BinaryOperator;

import org.apache.commons.lang3.tuple.Pair;

/**
 * <p>{@link QueryProcessorOptionReducer} that retain only a single value from the list of values.
 * The value to kept is selected via a {@link BinaryOperator} and is then converted to String to get the
 * final value.</p>
 * 
 * @author nguillaumin@funnelback.com
 *
 * @param <T> Values type
 */
public class KeepSingleValueReducer<T> extends AbstractQueryProcessorOptionReducer<T> {

    /**
     * <p>Function to use to select the value to keep.</p>
     * 
     * <p> From 2 values of T, return only one </p>
     **/
    private final BinaryOperator<T> reducingFunction;

    public KeepSingleValueReducer(BinaryOperator<T> reducingFunction, String... supportedOptions) {
        super(supportedOptions);
        this.reducingFunction = reducingFunction;
    }

    @Override
    public Pair<String, String> reduceInternal(String name, List<T> values) {
        T value = values.stream()
            .reduce(reducingFunction)
            .get();

        return Pair.of(name, value.toString());
    }

}
