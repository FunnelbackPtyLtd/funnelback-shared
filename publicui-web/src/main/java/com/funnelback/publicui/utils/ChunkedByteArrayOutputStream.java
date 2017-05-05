package com.funnelback.publicui.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;



/**
 * 
 */
public class ChunkedByteArrayOutputStream extends OutputStream {

    private static final int MAX_CHUNK_SIZE = 1 * 1204 * 1024; // 1MB
    private List<byte[]> arrays = new LinkedList<>();
    private ByteArrayOutputStream underlyingStream;
    private int sizeLimitPerChunk;
    
    public ChunkedByteArrayOutputStream(int initialSize) {
        this.sizeLimitPerChunk = initialSize;
        assert sizeLimitPerChunk > 0;
        this.underlyingStream = new ByteArrayOutputStream(sizeLimitPerChunk);
    }

    @Override
    public synchronized void write(int b) {
        if(0 == bytesLeft()) {
            streamFull();
        }
        underlyingStream.write(b);
        
    }

    @Override
    public synchronized void write(byte b[], int off, int len) {
        while(len > 0) {
            int left = bytesLeft();
            if(left == 0) {
                streamFull();
            } else {
                int bytesToWrite = Math.min(len, left);
                underlyingStream.write(b, off, bytesToWrite);
                len -= bytesToWrite;
                off += bytesToWrite;
            }
        }
    }

    private int bytesLeft() {
        return sizeLimitPerChunk - underlyingStream.size();
    }
    
    private void streamFull() {
        arrays.add(underlyingStream.toByteArray());
        if(sizeLimitPerChunk < MAX_CHUNK_SIZE) {
            // Make the underlying stream larger up to 1MB.
            sizeLimitPerChunk = Math.min(MAX_CHUNK_SIZE, sizeLimitPerChunk * 2);
            underlyingStream = new ByteArrayOutputStream(sizeLimitPerChunk);
        }
        underlyingStream.reset();
    }
    

    public void reset() {
        underlyingStream.reset();
        arrays = new LinkedList<>();
    }
    
    public int size() {
        return this.underlyingStream.size() + arrays.stream().mapToInt(a -> a.length).sum();
    }

    public InputStream toInputStream() {
        
        List<GCFriendlyInputStream> streams = new LinkedList<>();
        arrays.stream().map(SizeKnownByteArrayInputStream::new).map(GCFriendlyInputStream::new).forEach(streams::add);
        streams.add(new GCFriendlyInputStream(new SizeKnownByteArrayInputStream(underlyingStream.toByteArray())));
        
            
        return new SizeRecordingSequenceInputStream(streams);
    }
    
    /**
     * Gets all of the bytes written so far as an array.
     * 
     * <p>This is less efficent than using the toInputStream().</p>
     * 
     * @return
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[size()];
        int start = 0;
        for(int i = 0; i < arrays.size(); i++) {
            System.arraycopy(arrays.get(i), 0, bytes, start, arrays.get(i).length);
            start += arrays.get(i).length;
        }
        
        byte[] currentStreamArray = this.underlyingStream.toByteArray();
        System.arraycopy(currentStreamArray, 0, bytes, start, currentStreamArray.length);
        return bytes;
    }
    
    
    @AllArgsConstructor
    protected static class GCFriendlyInputStream extends InputStream {
        private SizeKnownByteArrayInputStream byteArrayInputStream;
        

        public int hashCode() {
            return byteArrayInputStream.hashCode();
        }

        public int read(byte[] b) throws IOException {
            return byteArrayInputStream.read(b);
        }

        public boolean equals(Object obj) {
            return byteArrayInputStream.equals(obj);
        }

        public int read() {
            return byteArrayInputStream.read();
        }

        public int read(byte[] b, int off, int len) {
            return byteArrayInputStream.read(b, off, len);
        }

        public long skip(long n) {
            return byteArrayInputStream.skip(n);
        }

        public int available() {
            return byteArrayInputStream.available();
        }

        public boolean markSupported() {
            return byteArrayInputStream.markSupported();
        }

        public void mark(int readAheadLimit) {
            byteArrayInputStream.mark(readAheadLimit);
        }

        public void reset() {
            byteArrayInputStream.reset();
        }

        public void close() throws IOException {
            byteArrayInputStream.close();
            byteArrayInputStream = null;
        }

        public String toString() {
            return byteArrayInputStream.toString();
        }
        
        /**
         * Returns the total size of the stream.
         * 
         * @return
         */
        public int getSize() {
            return byteArrayInputStream.getSize();
        }

        
        
    }
}
