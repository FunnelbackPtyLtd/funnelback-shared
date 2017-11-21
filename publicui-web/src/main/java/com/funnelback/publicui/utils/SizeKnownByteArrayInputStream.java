package com.funnelback.publicui.utils;

import java.io.ByteArrayInputStream;

import lombok.Getter;

public class SizeKnownByteArrayInputStream extends ByteArrayInputStream {

    @Getter private final int size;
    
    public SizeKnownByteArrayInputStream(byte[] array) {
        super(array);
        this.size = array.length;
    }
}
