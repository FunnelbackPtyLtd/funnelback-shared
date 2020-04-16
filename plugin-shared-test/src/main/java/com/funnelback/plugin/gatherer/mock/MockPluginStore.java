package com.funnelback.plugin.gatherer.mock;

import java.util.ArrayList;
import java.util.List;

import java.net.URI;

import com.funnelback.plugin.gatherer.PluginStore;
import com.google.common.collect.ListMultimap;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Stores records in memory, don't store too many documents into this.
 *
 */
public class MockPluginStore implements PluginStore {
    
    /**
     * 
     * 
     * Although this class is immutable, the fields are not.
     * 
     *
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockPluginStoreResult {
        private final URI uri;
        private final byte[] content;
        private final ListMultimap<String, String> metadata;
    }

    
    @Getter private final List<MockPluginStoreResult> stored = new ArrayList<>();
    
    @Override
    public void store(URI uri, byte[] content, ListMultimap<String, String> metadata) {
        this.stored.add(new MockPluginStoreResult(uri, content, metadata));
    }

}
