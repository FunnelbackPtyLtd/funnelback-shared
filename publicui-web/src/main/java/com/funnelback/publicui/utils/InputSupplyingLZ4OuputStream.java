package com.funnelback.publicui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

public class InputSupplyingLZ4OuputStream extends LZ4FrameOutputStream implements InputSupplyingOuputStream {

    private ByteArrayOutputStream bos;
    
    public InputSupplyingLZ4OuputStream(ByteArrayOutputStream bos) throws IOException {
        super(bos, BLOCKSIZE.SIZE_64KB);
        LZ4Factory.fastestJavaInstance().fastCompressor();
        this.bos = bos;
    }

    /**
     * Will not work unless the steam is closed!
     */
    @Override
    public Supplier<InputStream> asInputStream() {
        return new UncompressingStreamSupplier(bos.toByteArray());
    }
    
    @AllArgsConstructor
    private static class UncompressingStreamSupplier implements Supplier<InputStream> {

        private final byte[] bytes;
        
        @Override
        public InputStream get() {
            try {
                return new LZ4FrameInputStream(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    

}
