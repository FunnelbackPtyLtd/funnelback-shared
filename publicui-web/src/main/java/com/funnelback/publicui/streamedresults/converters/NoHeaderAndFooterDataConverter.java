package com.funnelback.publicui.streamedresults.converters;

import java.util.List;

import java.io.IOException;

import com.funnelback.publicui.streamedresults.DataConverter;


public class NoHeaderAndFooterDataConverter<T> extends DelegateDataConverter<T> {
    
    public NoHeaderAndFooterDataConverter(DataConverter<T> dataConverter) {
        super(dataConverter);
    }

    public void writeHead(List<String> fieldNames, T writer) throws IOException {
        // Don't write the header
    }

    public void writeFooter(T writer) throws IOException {
        // Don't write the footer
    }

}
