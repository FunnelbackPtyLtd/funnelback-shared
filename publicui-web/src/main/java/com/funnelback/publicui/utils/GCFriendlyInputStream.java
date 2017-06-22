package com.funnelback.publicui.utils;

import java.io.IOException;
import java.io.InputStream;

import lombok.AllArgsConstructor;

@AllArgsConstructor class GCFriendlyInputStream extends InputStream {
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