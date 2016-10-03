package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

/**
 * <p>{@link QueryProcessorOptionReducer} that converts all values to String, removes duplicates,
 * separate them with a delimiter (comma by default), and wraps them inside 2 characters
 * (<code>[,]</code> by default)<p>
 * 
 * <p>Suitable for <code>-rmcf</code>, <code>-SF</code>, etc.</p>
 * 
 * @author nguillaumin@funnelback.com
 *
 * @param <T> Values type
 */
public class WrappedDelimitedDistinctMultiValuesReducer<T> extends AbstractQueryProcessorOptionReducer<T> {

    /** Default wrapping characters */
    private static final Pair<String, String> DEFAULT_WRAPPERS = Pair.of("[", "]");

    /** Default delimiter */
    private static final String DEFAULT_DELIMITER = ",";

    /** Characters to wrap the final value in */
    private final Pair<String, String> wrappers = DEFAULT_WRAPPERS;

    /** Delimiter to separate values */
    private final String delimiter = DEFAULT_DELIMITER;

    public WrappedDelimitedDistinctMultiValuesReducer(String... supportedOptions) {
        super(supportedOptions);
    }

    @Override
    protected Pair<String, String> reduceInternal(String name, List<T> values) {
        String value = values.stream()
            .map(Object::toString)
            .distinct()
            .sorted() // For readability
            .collect(Collectors.joining(delimiter));

        return Pair.of(name, String.format("%s%s%s", wrappers.getLeft(), value, wrappers.getRight()));
    }

}
