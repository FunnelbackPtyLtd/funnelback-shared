package com.funnelback.publicui.streamedresults.converters;

import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonGenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GeneratorAndStream {
    private final JsonGenerator jsonGenerator;
    private final OutputStream outputStream;
}
