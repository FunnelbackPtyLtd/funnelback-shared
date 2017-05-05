package com.funnelback.publicui.utils;

import java.io.InputStream;
import java.nio.charset.Charset;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Return of an executed process
 */
@AllArgsConstructor
public class ExecutionReturn {
    @Getter private final int returnCode;
    
    @Getter private final InputStream outBytes;
    
    @Getter private final InputStream errBytes;
    
    @Getter private final int untruncatedOutputSize;
    
    /**
     * Charset of the bytes.
     */
    @Getter private final Charset charset;


}
