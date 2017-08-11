package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;

/**
 * Interacts with {@link QueryProcessorOptionReducer}s.
 * 
 * @author nguillaumin@funnelback.com
 *
 */
public class QueryProcessorOptionReducers {

    /**
     * List of all reducers we support
     */
    @SuppressWarnings("unchecked")
    public final static List<QueryProcessorOptionReducer<Object>> REDUCERS = Arrays.asList(new QueryProcessorOptionReducer[] {
                    new WrappedDelimitedDistinctMultiValuesReducer<>(QueryProcessorOptionKeys.RMCF),
                    new KeepSingleValueReducer<Integer>(Math::max, QueryProcessorOptionKeys.COUNT_URLS),
                    
                    // count_dates only supports the 'd' class
                    new KeepSingleValueReducer<>((a, b) -> "d", QueryProcessorOptionKeys.COUNT_DATES),
                    
                    // Naively set 'all' for countgbits if there are multiple specified
                    // Not worth the effort (?) to support complex cases of mixing 'all' with specific
                    // gscope numbers.
                    new KeepSingleValueReducer<>((a, b) -> "all", QueryProcessorOptionKeys.COUNTGBITS),
                    
                    // Counting of docs per collection is either on or off.
                    new KeepSingleValueReducer<>((a, b) -> true, QueryProcessorOptionKeys.DOCS_PER_COLLECTION),
                    
                    
    });

    /**
     * Reduces a list of heterogeneous query processor options into single (name, value) pair, using
     * the appropriate {@link QueryProcessorOptionReducer}s. If an option is not supported for
     * reducing, it's left as-is.
     * 
     * @param options Options to reduce
     * @return Reduced list of options
     */
    public List<Pair<String, String>> reduceAllQueryProcessorOptions(List<QueryProcessorOption<?>> options) {
        // Group options by name
        Map<String, List<QueryProcessorOption<?>>> groupedOptions = options.stream()
            .collect(Collectors.groupingBy(QueryProcessorOption::getName));

        return groupedOptions.entrySet()
            .stream()
            .flatMap(entry -> {
                String name = entry.getValue().get(0).getName();

                // Locate reducer supporting this option
                Optional<QueryProcessorOptionReducer<Object>> reducerOption = REDUCERS.stream()
                    .filter(reducer -> reducer.supports(name))
                    .findFirst();

                if (reducerOption.isPresent()) {
                    return Collections.singletonList(reducerOption.get().reduce(
                        entry.getKey(),
                        entry.getValue()
                            .stream()
                            .map(QueryProcessorOption::getValue)
                            .collect(Collectors.toList())))
                        .stream();
                } else {
                    // We don't know how to reduce this option, return all its different values as-is
                    // This may result in duplicate options
                    return entry.getValue()
                        .stream()
                        .map(option -> Pair.of(option.getName(), option.getValue().toString()))
                        .collect(Collectors.toList())
                        .stream();
                }
            })
            .collect(Collectors.toList());

    }

}
