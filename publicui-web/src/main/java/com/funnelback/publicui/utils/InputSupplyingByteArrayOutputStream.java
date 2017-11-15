package com.funnelback.publicui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

public class InputSupplyingByteArrayOutputStream extends ByteArrayOutputStream implements InputSupplyingOuputStream {

    public InputSupplyingByteArrayOutputStream(int initialBufferSize) {
        super(initialBufferSize);
    }
    
    public InputSupplyingByteArrayOutputStream() {
        super();
    }
    
    @Override
    public Supplier<InputStream> asInputStream() {
        return new ByteArrayStreamSupplier(this.toByteArray());
    }
    
    @AllArgsConstructor
    public static class ByteArrayStreamSupplier implements Supplier<InputStream> {
        
        private final byte[] bytes;
        
        @Override
        public InputStream get() {
            return new ByteArrayInputStream(bytes);
        }
    }

}
