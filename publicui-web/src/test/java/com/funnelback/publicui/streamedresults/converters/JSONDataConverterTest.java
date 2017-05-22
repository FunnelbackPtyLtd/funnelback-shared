package com.funnelback.publicui.streamedresults.converters;

import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;




public class JSONDataConverterTest {

    @Test
    public void test() throws Exception {
        JSONDataConverter jsonDataConverter = new JSONDataConverter();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator writter = jsonDataConverter.createWriter(bos);
        jsonDataConverter.writeHead(null, writter);
        jsonDataConverter.writeRecord(asList("k1", "k2"), asList("v1", "v2"), writter);
        jsonDataConverter.writeSeperator(writter);
        jsonDataConverter.writeRecord(asList("k1", "k2"), asList("v1", "v2"), writter);
        jsonDataConverter.writeFooter(writter);
        jsonDataConverter.finished(writter);
        Assert.assertEquals("[{\"k1\":\"v1\",\"k2\":\"v2\"},{\"k1\":\"v1\",\"k2\":\"v2\"}]", new String(bos.toByteArray()));
    }
    
}
