package com.funnelback.publicui.test.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.BoundedByteArrayOutputStream;

public class BoundedByteArrayOutputStreamTest {

    @Test
    public void testWithinCapacity() throws IOException {
        BoundedByteArrayOutputStream os = new BoundedByteArrayOutputStream(10, 20);
        
        byte[] data = new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
        
        os.write(data);
        
        Assert.assertArrayEquals(os.toByteArray(), data);
    }

    @Test(expected=IOException.class)
    public void testExceedCapactiy() throws IOException {
        BoundedByteArrayOutputStream os = new BoundedByteArrayOutputStream(10, 20);
        
        byte[] data = new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,21};
        
        os.write(data);
        
        Assert.fail(); // Should never be reached due to exception from write above
    }

}
