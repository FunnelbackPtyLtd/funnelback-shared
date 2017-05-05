package com.funnelback.publicui.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;



public class ChunkedByteArrayOutputStreamTest {

    @Test @Ignore
    public void foo() throws Exception {
        int count = 0;
        
        InputStream is = createInputStream(1000, 512*1024);
        System.gc();
        while(is.read() != -1) {
            count++;
            if((count % (10 * 1024*1024)) == 0) {
                System.out.println("Total size read so far is " + count / 1024/1024 + " MB");
                System.in.read();
                System.gc();
            }
        }
        
        System.out.println("Total size read is " + count / 1024/1024 + " MB");
        System.in.read();
    }
    
    
    @Test
    public void memoryTest() throws Exception {
        int countOfBlocks = 100;
        int blockSize = 1024*1024;
        int iterations = 500;
        
        System.out.println("Total memory needed if not freeing memory: " + (1L * countOfBlocks * blockSize * iterations) /1024/1024/1024 + " GB");
        
        byte[] buf = new byte[4*1024];
        
        java.util.List<InputStream> inputStreams = new ArrayList<>(iterations);
        
        for(int i = 0; i < iterations; i++) {
            System.out.println(i + " out of " + iterations);
            InputStream is = createInputStream(100, 1024*1024);
            while(is.read(buf) != -1) {}
            inputStreams.add(is);
        }
        
    }
    
    public InputStream createInputStream(int count, int blockSize) throws IOException {
        ChunkedByteArrayOutputStream os = new ChunkedByteArrayOutputStream(1 * 1024 * 1204);
        byte[] bytes = new byte[blockSize];
        for(int i = 0; i < blockSize; i++) {
            bytes[i] = (byte)(i % 10 + 'a');
        }
        for(int i = 0; i < count; i++) {
            os.write(bytes);
        }
        return os.toInputStream();
    }
}
