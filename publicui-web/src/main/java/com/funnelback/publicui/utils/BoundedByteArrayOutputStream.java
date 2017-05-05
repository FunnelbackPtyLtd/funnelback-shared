package com.funnelback.publicui.utils;

import java.io.OutputStream;

import lombok.Getter;

/**
 * A ByteArrayOutputStream with a limited size,
 * which can be used to cap memory usage.
 * 
 * If the given limit is exceeded, further write
 * calls will be silently dropped.
 * 
 * Note - To simplify implementation, the size limit is
 * checked only before each new write call, so it is possible
 * for the limit to be exceeded by the size of the largest
 * single call to a write method in the worst case.
 */
public class BoundedByteArrayOutputStream extends OutputStream {

    @Getter private final ChunkedByteArrayOutputStream underlyingStream;
    private final int sizeLimit;
    private boolean isTruncated = false;

    /** The size the output would have been if it had not been truncated */
    @Getter
    private long untruncatedSize;
    
    public BoundedByteArrayOutputStream(ChunkedByteArrayOutputStream chunkedByteArrayOutputStream, int sizeLimit) {
        underlyingStream = chunkedByteArrayOutputStream;
        this.sizeLimit = sizeLimit;
    }

    @Override
    public synchronized void write(int b) {
        untruncatedSize += 1;
        if (!isTruncated && sizeIsBelowLimit()) {
            underlyingStream.write(b);
        } else {
            isTruncated = true;
        }
    }

    @Override
    public synchronized void write(byte b[], int off, int len) {
        untruncatedSize += len;
        if (!isTruncated && sizeIsBelowLimit()) {
            underlyingStream.write(b, off, len);
        } else {
            isTruncated = true;
        }
    }

    private boolean sizeIsBelowLimit() {
        return underlyingStream.size() < sizeLimit;
    }

    public void reset() {
        underlyingStream.reset();
    }

    /**
     * Return whether there have been write operations in excess
     * of the sizeLimit, meaning that the result of toByteArray
     * will be truncated compared to the source.
     */
    public boolean isTruncated() {
        return isTruncated;
    }
}
