package com.funnelback.publicui.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A ByteArrayOutputStream with a limted size,
 * which can be used to cap memory usage.
 * 
 * If the given limit is exceeded, an IOException will
 * be thrown
 */
public class BoundedByteArrayOutputStream extends OutputStream {

    private final ByteArrayOutputStream underlyingStream;
    private final int sizeLimit;
    
    public BoundedByteArrayOutputStream(int initialSize, int sizeLimit) {
        underlyingStream = new ByteArrayOutputStream(initialSize);
        this.sizeLimit = sizeLimit;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        underlyingStream.write(b);
        checkSize();
    }

    @Override
    public synchronized void write(byte b[], int off, int len) throws IOException {
        underlyingStream.write(b, off, len);
        checkSize();
    }

    private void checkSize() throws IOException {
        if (underlyingStream.size() > sizeLimit) {
            throw new IOException("Cannot accept data beyond the size limit of " + sizeLimit + " bytes");
        }
    }

    public void reset() {
        underlyingStream.reset();
    }

    public byte toByteArray()[] {
        return underlyingStream.toByteArray();
    }

}
