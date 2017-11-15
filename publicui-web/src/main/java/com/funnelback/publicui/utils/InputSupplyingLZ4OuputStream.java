package com.funnelback.publicui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;
import java.util.zip.Checksum;

import org.apache.commons.exec.OS;

import lombok.AllArgsConstructor;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

public class InputSupplyingLZ4OuputStream implements InputSupplyingOuputStream {

    /**
     * Same seed used by lz4 java library
     */
    static final int DEFAULT_SEED = 0x9747b28c;
    
    private static final LZ4Factory COMPRESSOR_FACTORY = getLZ4CompressorFactory();
    
    private static LZ4Factory getLZ4CompressorFactory() {
        // Don't use the fastest c implementations on windows as the files will not be cleaned up
        // although maybe it will because jetty has its own tmp dir
        if(OS.isFamilyWindows()) {
            return LZ4Factory.fastestJavaInstance();
        }
        return LZ4Factory.fastestInstance();
    }
    
    private static final XXHashFactory XX_HASH_FACTORY = getXXHashFactory();
    
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
    
    
    private ByteArrayOutputStream bos;
    private OutputStream os;
    
    

    public InputSupplyingLZ4OuputStream(ByteArrayOutputStream bos) throws IOException {
        this.bos = bos;
        this.os = new LZ4BlockOutputStream(bos, 1 << 16, COMPRESSOR_FACTORY.fastCompressor(), getChecksum(), false);
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
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public int hashCode() {
        return os.hashCode();
    }

    @Override
    public void write(byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    @Override
    public boolean equals(Object obj) {
        return os.equals(obj);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
    
    @Override
    public OutputStream asOutputStream() {
        return os;
    }
}
