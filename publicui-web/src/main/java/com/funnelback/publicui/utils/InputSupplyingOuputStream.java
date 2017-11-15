package com.funnelback.publicui.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface InputSupplyingOuputStream extends AutoCloseable {

    public abstract void write(int b) throws IOException;
    public void write(byte b[]) throws IOException;
    public void write(byte b[], int off, int len) throws IOException;    
    public void flush() throws IOException;
    public void close() throws IOException;
    
    public InputStream asInputStream();
    
    public default OutputStream asOutputStream() {
        InputSupplyingOuputStream sizedOuputStream = this;
        new ByteArrayOutputStream().reset();
        return new OutputStream() {
            
            private final InputSupplyingOuputStream underlyingStream = sizedOuputStream;

            public void write(int b) throws IOException {
                underlyingStream.write(b);
            }

            public void write(byte[] b) throws IOException {
                underlyingStream.write(b);
            }

            public void write(byte[] b, int off, int len) throws IOException {
                underlyingStream.write(b, off, len);
            }

            public void flush() throws IOException {
                underlyingStream.flush();
            }

            public void close() throws IOException {
                underlyingStream.close();
            }
        };
        
    }

}
