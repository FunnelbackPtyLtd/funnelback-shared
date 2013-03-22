package com.funnelback.publicui.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Return of an executed process
 */
@RequiredArgsConstructor
public class ExecutionReturn {
    @Getter private final int returnCode;
    @Getter private final String output;
}
