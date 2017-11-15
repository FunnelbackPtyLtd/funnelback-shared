package com.funnelback.publicui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class InputSupplyingByteArrayOutputStream extends ByteArrayOutputStream implements InputSupplyingOuputStream {

    public InputSupplyingByteArrayOutputStream(int initialBufferSize) {
        super(initialBufferSize);
    }
    
    public InputSupplyingByteArrayOutputStream() {
        super();
    }
    
    @Override
    public InputStream asInputStream() {
        return new ByteArrayInputStream(this.toByteArray());
    }

}
