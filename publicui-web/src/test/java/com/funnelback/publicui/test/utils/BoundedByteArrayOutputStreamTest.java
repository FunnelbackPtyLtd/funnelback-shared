package com.funnelback.publicui.test.utils;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.BoundedByteArrayOutputStream;

public class BoundedByteArrayOutputStreamTest {

    private byte[] testData = new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
    
    @Test
    public void testWithinCapacity() throws IOException {
        try (BoundedByteArrayOutputStream os = new BoundedByteArrayOutputStream(10, 20)) {
            os.write(testData);
            
            Assert.assertFalse("Expected output stream not to be truncated", os.isTruncated());
            Assert.assertArrayEquals(os.toByteArray(), testData);
            Assert.assertEquals("Expected correct untruncated length", os.getUntruncatedSize(), testData.length);
        }
    }

    @Test
    public void testExceedCapactiy() throws IOException {
        try (BoundedByteArrayOutputStream os = new BoundedByteArrayOutputStream(10, 20)) {
            os.write(testData);
            os.write(testData);
            
            Assert.assertTrue("Expected output stream to be truncated", os.isTruncated());
            Assert.assertEquals("Expected correct untruncated length", os.getUntruncatedSize(), testData.length * 2);
        }
    }

}
