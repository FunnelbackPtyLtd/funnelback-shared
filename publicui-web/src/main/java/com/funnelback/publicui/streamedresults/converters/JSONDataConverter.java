package com.funnelback.publicui.streamedresults.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnelback.publicui.streamedresults.DataConverter;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Component
public class JSONDataConverter implements DataConverter<JsonGenerator> {

    /** Use the same object mapper used by search.json */
    @Autowired
    @Qualifier("jackson2ObjectMapper")
    private ObjectMapper jacksonObjectMapper;
    
    
    @Override
    public String getContentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public JsonGenerator createWritter(OutputStream outputStream) throws IOException {
        return jacksonObjectMapper.getFactory().createGenerator(outputStream);
    }

    @Override
    public void writeHead(List<String> fieldNames, JsonGenerator writer) throws IOException {
        writer.writeStartArray();
    }

    @Override
    public void writeRecord(List<String> fieldNames, List<Object> values, JsonGenerator writer) throws IOException {
        Map<String, Object> map = new HashMap<>();
        for(int i = 0; i < fieldNames.size(); i++) {
            map.put(fieldNames.get(i), values.get(i));
        }
        writer.writeObject(map);
    }

    @Override
    public void writeSeperator(JsonGenerator writer) throws IOException {
    }

    @Override
    public void writeFooter(JsonGenerator writer) throws IOException {
        writer.writeEndArray();
    }

    @Override
    public void finished(JsonGenerator writer) throws IOException {
        writer.close();
    }

    


}
