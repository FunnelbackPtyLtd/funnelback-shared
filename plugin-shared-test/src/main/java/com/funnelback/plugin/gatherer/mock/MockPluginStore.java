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
 * A mock PluginStore that may be used when testing the PluginGatherer.
 * 
 * Example:
 * <code>
 * MockPluginStore pluginStore = new MockPluginStore();
 * 
 * MockPluginGatherContext pluginGatherContext = new MockPluginGatherContext();
 * 
 * PluginGatherer underTest = new MyPluginGatherer();
 * 
 * underTest.gather(pluginGatherContext, pluginStore);
 * 
 * Assert.assertEquals("2 documents should have been gathered.", 2, pluginStore.getStored().size());
 * 
 * Assert.assertEquals("http://example.com/1", pluginStore.getStored().get(0).getUri().toASCIIString());
 * Assert.assertEquals("http://example.com/2", pluginStore.getStored().get(1).getUri().toASCIIString());
 * </code> 
 * 
 * Stores records in memory, don't store too many documents into this.
 * 
 * 
 *
 */
public class MockPluginStore implements PluginStore {
    
    /**
     * Holds the values that {@link MockPluginStore#store(URI, byte[], ListMultimap)} was called with.
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
    public synchronized void store(URI uri, byte[] content, ListMultimap<String, String> metadata) {
        this.stored.add(new MockPluginStoreResult(uri, content, metadata));
    }

}
