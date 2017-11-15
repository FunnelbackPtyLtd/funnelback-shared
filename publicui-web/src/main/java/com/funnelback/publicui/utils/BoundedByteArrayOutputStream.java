package com.funnelback.publicui.utils;

import java.io.IOException;
import java.io.InputStream;

import lombok.AllArgsConstructor;
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
public class BoundedByteArrayOutputStream extends SizeListeningOutputStream {
    
    private final int sizeLimit;
    
    /**
     * Return whether there have been write operations in excess
     * of the sizeLimit, meaning that the result of toByteArray
     * will be truncated compared to the source.
     */
    @Getter private boolean isTruncated = false;
    
    public BoundedByteArrayOutputStream(InputSupplyingOuputStream underlyingStream, int sizeLimit) {
        super(underlyingStream);
        this.sizeLimit = sizeLimit;
    }
    
    @Override
    public void beforeWrite(long sizeAfterProposedWrite) {
        if(isTruncated) {
            return;
        }
        
        if(sizeAfterProposedWrite > sizeLimit) {
            this.setUnderlyingStream(new NoMoreWritesOutputStream(this.getUnderlyingStream()));
            isTruncated = true;
        }
        
    }
    /** 
     * The size the output would have been if it had not been truncated 
     */
    public long getUntruncatedSize() {
        return super.getTotalBytesGiven();
    }
    
    @AllArgsConstructor
    private static class NoMoreWritesOutputStream implements InputSupplyingOuputStream {
        
        InputSupplyingOuputStream sizedOuputStream;

        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
            this.sizedOuputStream.flush();
        }

        @Override
        public void close() throws IOException {
            this.sizedOuputStream.close();
        }

        @Override
        public InputStream asInputStream() {
            return this.sizedOuputStream.asInputStream();
        }
        
    }
    
}