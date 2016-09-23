package com.funnelback.publicui.utils;

import java.nio.charset.Charset;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Return of an executed process
 */
@RequiredArgsConstructor
public class ExecutionReturn {
    @Getter private final int returnCode;
    
    @Getter private final byte[] outBytes;
    
    @Getter private final byte[] errBytes;
    
    /**
     * Charset of the bytes.
     */
    @Getter private final Charset charset;
}
