package com.funnelback.publicui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;
import java.util.zip.Checksum;

import org.apache.commons.exec.OS;

import com.funnelback.publicui.utils.InputSupplyingOuputStream.DelegateInputSupplyingOuputStream;

import lombok.AllArgsConstructor;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.xxhash.XXHashFactory;

public class InputSupplyingLZ4OuputStream {

    /**
     * Same seed used by lz4 java library
     */
    static final int DEFAULT_SEED = 0x9747b28c;

    private static final LZ4Factory COMPRESSOR_FACTORY = getLZ4CompressorFactory();
    
    private static final XXHashFactory XX_HASH_FACTORY = getXXHashFactory();

    public static InputSupplyingOuputStream getCompressedStream(ByteArrayOutputStream bos) {
        try {
            if(OS.isFamilyWindows()) {
                return new InputSupplyingBlockLZ4OuputStream(bos);
            }
            return new InputSupplyingFrameLZ4OuputStream(bos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static LZ4Factory getLZ4CompressorFactory() {
        // Don't use the fastest c implementations on windows as the files will not be cleaned up
        // although maybe it will because jetty has its own tmp dir
        if(OS.isFamilyWindows()) {
            return LZ4Factory.fastestJavaInstance();
        }
        return LZ4Factory.fastestInstance();
    }

    

    private static XXHashFactory getXXHashFactory() {
        // Don't use the fastest c implementations on windows as the files will not be cleaned up
        // although maybe it will because jetty has its own tmp dir
        if(OS.isFamilyWindows()) {
            return XXHashFactory.fastestJavaInstance();
        }
        return XXHashFactory.fastestInstance();
    }
    
    

    private static Checksum getChecksum() {
        return XX_HASH_FACTORY.newStreamingHash32(DEFAULT_SEED).asChecksum();
    }
    
    
    
    /**
     * This is faster than the block output stream (and is also the recommended stream to use)
     * 
     * However it can only be used with Linux because I think on windows this might cause some issues with left over files.
     *
     */
    static class InputSupplyingFrameLZ4OuputStream extends LZ4FrameOutputStream implements InputSupplyingOuputStream {

        private ByteArrayOutputStream bos;
        
        public InputSupplyingFrameLZ4OuputStream(ByteArrayOutputStream out) throws IOException {
            super(out, BLOCKSIZE.SIZE_64KB);
            bos = out;
        }
        
        @Override
        public Supplier<InputStream> asInputStream() {
            byte[] bytes = bos.toByteArray();
            return new UncompressingStreamSupplier(bytes);
        }
        
        @Override
        public OutputStream asOutputStream() {
            return this;
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
    
    /**
     * Have to use this under windows because we can not set the compressor to use.
     *
     */
    static class InputSupplyingBlockLZ4OuputStream extends DelegateInputSupplyingOuputStream implements InputSupplyingOuputStream {
        private ByteArrayOutputStream bos;
        
        public InputSupplyingBlockLZ4OuputStream(ByteArrayOutputStream bos) throws IOException {
            super(new LZ4BlockOutputStream(bos, 1 << 16, COMPRESSOR_FACTORY.fastCompressor(), getChecksum(), false));
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
                return new LZ4BlockInputStream(new ByteArrayInputStream(bytes), COMPRESSOR_FACTORY.fastDecompressor(), getChecksum());
            }

        }

        @Override
        public OutputStream asOutputStream() {
            return os;
        }
    }



}
