package com.funnelback.publicui.streamedresults.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.funnelback.publicui.streamedresults.DataConverter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JSONPDataConverter implements DataConverter<GeneratorAndStream>{

    private final String callback;
    private final JSONDataConverter jsonDataConverter;
    
    
    @Override
    public String getContentType() {
        return "application/javascript";
    }

    @Override
    public GeneratorAndStream createWriter(OutputStream outputStream) throws IOException {
        GeneratorAndStream generatorAndStream = new GeneratorAndStream(jsonDataConverter.createWriter(outputStream), outputStream);
        generatorAndStream.getJsonGenerator().disable(Feature.AUTO_CLOSE_TARGET);
        return generatorAndStream;
    }

    @Override
    public void writeHead(List<String> fieldNames, GeneratorAndStream writer) throws IOException {
        writer.getOutputStream().write((callback + "(").getBytes(StandardCharsets.UTF_8));
        jsonDataConverter.writeHead(fieldNames, writer.getJsonGenerator());
    }

    @Override
    public void writeRecord(List<String> fieldNames, List<Object> values, GeneratorAndStream writer)
        throws IOException {
        jsonDataConverter.writeRecord(fieldNames, values, writer.getJsonGenerator());
    }

    @Override
    public void writeSeperator(GeneratorAndStream writer) throws IOException {
        jsonDataConverter.writeSeperator(writer.getJsonGenerator());
    }

    @Override
    public void writeFooter(GeneratorAndStream writer) throws IOException {
        jsonDataConverter.writeFooter(writer.getJsonGenerator());
    }

    @Override
    public void finished(GeneratorAndStream writer) throws IOException {
        jsonDataConverter.finished(writer.getJsonGenerator());
        writer.getOutputStream().write(")".getBytes(StandardCharsets.UTF_8));
        writer.getOutputStream().close();
    }

    

    

}
