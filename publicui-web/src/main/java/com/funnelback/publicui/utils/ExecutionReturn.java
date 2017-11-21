package com.funnelback.publicui.utils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Return of an executed process
 */
@AllArgsConstructor
public class ExecutionReturn {
    @Getter private final int returnCode;
    
    @Getter private final Supplier<InputStream> outBytes;
    
    @Getter private final Supplier<InputStream> errBytes;
    
    @Getter private final int untruncatedOutputSize;
    
    /**
     * Charset of the bytes.
     */
    @Getter private final Charset charset;


}
