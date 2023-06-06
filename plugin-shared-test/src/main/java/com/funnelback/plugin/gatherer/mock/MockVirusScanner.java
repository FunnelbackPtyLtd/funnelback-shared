package com.funnelback.plugin.gatherer.mock;

import com.funnelback.plugin.gatherer.VirusScanner;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * A Mock VirusScanner that may be used when testing the PluginGatherer.
 *
 */
public class MockVirusScanner implements VirusScanner{

    @Getter
    @Setter
    private boolean checkResults = false;

    @Override
    public boolean checkbytes(byte[] content) {
        return checkResults;
    }
}
