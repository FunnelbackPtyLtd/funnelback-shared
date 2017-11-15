package com.funnelback.publicui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

public class InputSupplyingLZ4OuputStream extends LZ4FrameOutputStream implements InputSupplyingOuputStream {

    private ByteArrayOutputStream bos;
    
    public InputSupplyingLZ4OuputStream(ByteArrayOutputStream bos) throws IOException {
        super(bos, BLOCKSIZE.SIZE_256KB);
        LZ4Factory.fastestJavaInstance().fastCompressor();
        this.bos = bos;
    }

    /**
     * Will not work unless the steam is closed!
     */
    @Override
    public InputStream asInputStream() {
        try {
            return new LZ4FrameInputStream(new ByteArrayInputStream(bos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    

}
