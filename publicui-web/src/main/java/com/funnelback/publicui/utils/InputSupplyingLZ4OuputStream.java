package com.funnelback.publicui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

public class InputSupplyingLZ4OuputStream implements InputSupplyingOuputStream {

    static final int DEFAULT_SEED = 0x9747b28c;
    
    private ByteArrayOutputStream bos;
    private OutputStream os;
    
    

    public InputSupplyingLZ4OuputStream(ByteArrayOutputStream bos) throws IOException {
        this.bos = bos;
        this.os = new LZ4BlockOutputStream(bos, 1 << 16, 
            LZ4Factory.fastestJavaInstance().fastCompressor(),
            XXHashFactory.fastestJavaInstance().newStreamingHash32(DEFAULT_SEED).asChecksum(), false);
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
            return new LZ4BlockInputStream(new ByteArrayInputStream(bytes));
        }
        
    }

    public void write(int b) throws IOException {
        os.write(b);
    }

    public int hashCode() {
        return os.hashCode();
    }

    public void write(byte[] b) throws IOException {
        os.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    public boolean equals(Object obj) {
        return os.equals(obj);
    }

    public void flush() throws IOException {
        os.flush();
    }

    public void close() throws IOException {
        os.close();
    }
    
    @Override
    public OutputStream asOutputStream() {
        return os;
    }
}
