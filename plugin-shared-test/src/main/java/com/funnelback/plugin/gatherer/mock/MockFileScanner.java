package com.funnelback.plugin.gatherer.mock;

import com.funnelback.plugin.gatherer.FileScanner;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * A Mock FileScanner that may be used when testing the PluginGatherer.
 *
 */
public class MockFileScanner implements FileScanner {

    @Getter
    @Setter
    private boolean checkResults = false;

    @Override
    public boolean checkbytes(byte[] content) {
        return checkResults;
    }
}
