package com.funnelback.publicui.streamedresults.converters;

import com.funnelback.publicui.streamedresults.DataConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;

@AllArgsConstructor
public class DelegateDataConverter<T> implements DataConverter<T> {
    
    /**
     * All requests are forwarded to this.
     */
    @Delegate @Getter private final DataConverter<T> dataConverter;
}
