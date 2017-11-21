package com.funnelback.publicui.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

/**
 * An interface for a InputStream which has the ability to re-fetched the written bytes 
 * in the form of a input stream.
 * 
 * <p>The interface seemed need to be able to compose classes</p>
 * 
 */
public interface InputSupplyingOuputStream extends Closeable {

    public abstract void write(int b) throws IOException;
    public void write(byte b[]) throws IOException;
    public void write(byte b[], int off, int len) throws IOException;    
    public void flush() throws IOException;
    public void close() throws IOException;
   
    /**
     * Gets a supplier that will provide input streams of the written bytes.
     * 
     * <p>Some implementations will require that close() be called before the 
     * result of this is valid.<p>
     * 
     * @return
     */
    public Supplier<InputStream> asInputStream();
    
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
    
    /**
     * Helper class to wrap a OuputStream to a InputSupplyingOuputStream
     *
     */
    @AllArgsConstructor
    public abstract class DelegateInputSupplyingOuputStream implements InputSupplyingOuputStream {
        protected final OutputStream os;

        public void write(int b) throws IOException {
            os.write(b);
        }

        public void write(byte[] b) throws IOException {
            os.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            os.write(b, off, len);
        }

        public void flush() throws IOException {
            os.flush();
        }

        public void close() throws IOException {
            os.close();
        }

        
    }

}
