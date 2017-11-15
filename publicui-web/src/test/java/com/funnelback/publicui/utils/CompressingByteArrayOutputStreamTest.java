package com.funnelback.publicui.utils;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;



public class CompressingByteArrayOutputStreamTest {
    
    @Test
    public void roundTrip() throws Exception {
        CompressingByteArrayOutputStream outputStream = CompressingByteArrayOutputStream.compressAfter(1);
        outputStream.write("hello".getBytes());
        outputStream.close();
        String s = new String(IOUtils.toByteArray(outputStream.asInputStream()));
        Assert.assertEquals("hello", s);
    }
    
    
    
    
}
