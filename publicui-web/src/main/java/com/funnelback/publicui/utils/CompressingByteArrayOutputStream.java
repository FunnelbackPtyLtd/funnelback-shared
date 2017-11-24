package com.funnelback.publicui.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.jfree.util.Log;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CompressingByteArrayOutputStream extends SizeListeningOutputStream {

    /**
     * Just under 8MB this is a good size because the underlying byte array output stream
     * will want to grow to 16MB (creating 16MB and holding onto the 8MB existing buf)
     * instead we switch to using the compressed stream which (if compressible) we will 
     * use less memory.
     */
    public static final int DEFAULT_COMPRESS_AFTER_SIZE = 8 * 1024 * 1024 - 1;
    
    private final int compressAfterSize;
    private boolean isCompressing = false;
    private boolean triedCompressing = false;
    
    private boolean closed = false;
    
    /**
     * We don't want to make public the constructor because if the user passes in the underlying stream
     * e.g.
     * new CompressingByteArrayOutputStream(new MyStream(), 123);
     * the instance of MyStream is held onto which is probably an unintended memory leak (not that the leak would be big)
     * 
     * @param sizedOuputStream
     * @param compressAfterSize Starts compressing the stream once this number of bytes have been reached.
     */
    private CompressingByteArrayOutputStream(InputSupplyingOuputStream underlyingStream, int compressAfterSize) {
        super(underlyingStream);
        this.compressAfterSize = compressAfterSize;
    }
    
    @Override
    public void close() throws IOException {
        closed = true;
        super.close();
    }
    
    @Override
    public Supplier<InputStream> asInputStream() {
        if(!closed) {
            throw new IllegalStateException("Stream attempt to be read before it was closed.");
        }
        return super.asInputStream();
    }

    @Override
    public void beforeWrite(long sizeAfterProposedWrite) {
        if(isCompressing) {
            return;
        }
        
        if(sizeAfterProposedWrite > compressAfterSize && ! triedCompressing) {
            triedCompressing = true;
            log.trace("Will attempt compressing the stream.");
            try {
                InputSupplyingOuputStream compressedStream = InputSupplyingLZ4OuputStream.getCompressedStream(new ByteArrayOutputStream());
                this.getUnderlyingStream().close();
                try {
                    IOUtils.copy(this.getUnderlyingStream().asInputStream().get(), compressedStream.asOutputStream());
                } catch (Throwable e) {
                    compressedStream.close();
                    throw e;
                }
                //All must be well, we will now be writing to the compressed stream.
                this.setUnderlyingStream(compressedStream);
                isCompressing = true;
            } catch (IOException e) {
                Log.warn("Could not compress stream", e);
            }
        }
    }
    
    public static CompressingByteArrayOutputStream compressAfter(int compressAfterSize) {
        return new CompressingByteArrayOutputStream(new InputSupplyingByteArrayOutputStream(), compressAfterSize);
    }
    
    public static Builder builder() {
        return new Builder(); 
    }
    
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Builder {
        @Wither private int initialByteArraySize = 32;
        @Wither private int compressAfterSize = DEFAULT_COMPRESS_AFTER_SIZE;
        
        public CompressingByteArrayOutputStream build() {
            return new CompressingByteArrayOutputStream(new InputSupplyingByteArrayOutputStream(initialByteArraySize), compressAfterSize);
        }
    }
}
