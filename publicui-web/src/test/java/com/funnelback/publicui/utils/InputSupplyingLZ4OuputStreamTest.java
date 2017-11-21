package com.funnelback.publicui.utils;

import java.io.ByteArrayOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import com.funnelback.publicui.utils.InputSupplyingLZ4OuputStream.InputSupplyingFrameLZ4OuputStream;
import com.funnelback.publicui.utils.InputSupplyingLZ4OuputStream.InputSupplyingBlockLZ4OuputStream;


public class InputSupplyingLZ4OuputStreamTest {

    @Test
    public void testLz4Frame() throws Exception {
        testRoundTrip(new InputSupplyingFrameLZ4OuputStream(new ByteArrayOutputStream()));
    }
    
    @Test
    public void testLz4Block() throws Exception {
        testRoundTrip(new InputSupplyingBlockLZ4OuputStream(new ByteArrayOutputStream()));
    }
    
    public void testRoundTrip(InputSupplyingOuputStream outputStream) throws Exception {
        outputStream.write("hello".getBytes());
        outputStream.close();
        String s = new String(IOUtils.toByteArray(outputStream.asInputStream().get()));
        Assert.assertEquals("hello", s);
    }
}

