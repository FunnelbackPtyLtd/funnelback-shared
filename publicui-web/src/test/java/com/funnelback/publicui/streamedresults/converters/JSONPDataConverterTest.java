package com.funnelback.publicui.streamedresults.converters;

import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONPDataConverterTest {

    @Test
    public void test() throws IOException {
        JSONPDataConverter jsonPDataConverter = new JSONPDataConverter("callback", new JSONDataConverter(new ObjectMapper()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GeneratorAndStream writter = jsonPDataConverter.createWriter(bos);
        jsonPDataConverter.writeHead(null, writter);
        jsonPDataConverter.writeRecord(asList("k1", "k2"), asList("v1", "v2"), writter);
        jsonPDataConverter.writeSeperator(writter);
        jsonPDataConverter.writeRecord(asList("k1", "k2"), asList("v1", "v2"), writter);
        jsonPDataConverter.writeFooter(writter);
        jsonPDataConverter.finished(writter);
        Assert.assertEquals("callback([{\"k1\":\"v1\",\"k2\":\"v2\"},{\"k1\":\"v1\",\"k2\":\"v2\"}])", new String(bos.toByteArray()));
    }
}
