package com.funnelback.publicui.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public abstract class SizeListeningOutputStream implements InputSupplyingOuputStream {
    
    
    @Setter @Getter private InputSupplyingOuputStream underlyingStream;
    @Getter(value=AccessLevel.PROTECTED) private long totalBytesGiven = 0;
    
    public SizeListeningOutputStream(InputSupplyingOuputStream sizedOuputStream) {
        this.underlyingStream = sizedOuputStream;
    }
    
    
        
    public abstract void beforeWrite(long sizeAfterProposedWrite);
    
    @Override
    public synchronized void write(int b) throws IOException {
        totalBytesGiven += 1;
        beforeWrite(totalBytesGiven);
        underlyingStream.write(b);
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        totalBytesGiven += b.length;
        beforeWrite(totalBytesGiven);
        this.underlyingStream.write(b);
    }

    @Override
    public synchronized void write(byte b[], int off, int len) throws IOException {
        totalBytesGiven += len;
        beforeWrite(totalBytesGiven);
        underlyingStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        underlyingStream.flush();
    }

    @Override
    public void close() throws IOException {
        underlyingStream.close();
    }
    
    @Override
    public Supplier<InputStream> asInputStream() {
        return underlyingStream.asInputStream();
    }
}
