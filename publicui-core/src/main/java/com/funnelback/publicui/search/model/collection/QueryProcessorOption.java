package com.funnelback.publicui.search.model.collection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A PADRE query processor option, with a name and a value
 * 
 * @author nguillaumin@funnelback.com
 *
 * @param <T> Type of value
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public class QueryProcessorOption<T> {
    
    /** QPO name */
    @Getter
    private final String name;
    
    /** QPO value */
    @Getter
    private final T value;
    
    /**
     * Used for debugging display, <b>not suitable</b> to use
     * to generate the CLI string representation.
     */
    @Override
    public String toString() {
        return String.format("'%s' = '%s'", name, value.toString());
    }
    
}
