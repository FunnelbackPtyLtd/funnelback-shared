package com.funnelback.publicui.streamedresults.converters;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.apache.commons.csv.CSVPrinter;
import org.junit.Assert;
import org.junit.Test;

public class CSVDataConverterTest {

    private final CSVDataConverter csvConverter = new CSVDataConverter();
    
    @Test
    public void test() throws Exception {
        ByteArrayOutputStream bos = spy(new ByteArrayOutputStream());
        CSVPrinter printer = csvConverter.createWriter(bos);
        csvConverter.writeHead(asList("foo", "bar"), printer);
        csvConverter.writeRecord(asList(), asList("foobar三つ", new Date(12367L)), printer);
        csvConverter.writeSeperator(printer);
        csvConverter.writeRecord(asList(), asList("foobar三つ", new Date(12367L)), printer);
        csvConverter.writeFooter(printer);
        csvConverter.finished(printer);
        
        // Check we closed the printer, check by ensuring the bos was closed.
        verify(bos).close();
        
        String s = new String(bos.toByteArray(), "UTF-8");
        
        System.out.println(s);
        
        Assert.assertEquals("foo,bar\r\n" + 
            "foobar三つ,12367\r\n" +  // NOTE the Date here is in ms since the epoch like search.json 
            "foobar三つ,12367\r\n" 
            , s);
    }
}
