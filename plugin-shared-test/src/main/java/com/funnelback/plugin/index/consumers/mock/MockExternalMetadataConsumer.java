package com.funnelback.plugin.index.consumers.mock;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.ExternalMetadataConsumer;
import com.google.common.collect.ListMultimap;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A mock {@link ExternalMetadataConsumer} that may be used when testing {@link IndexingConfigProvider#externalMetadata(IndexConfigProviderContext, ExternalMetadataConsumer)}.
 * 
 * Example:
 * <pre>{@code 
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockExternalMetadataConsumer mockConsumer = new MockExternalMetadataConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.externalMetadata(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * }</pre>
 */
public class MockExternalMetadataConsumer implements ExternalMetadataConsumer {

    /**
     * Holds the values that {@link MockExternalMetadataConsumer#addMetadataToPrefix(String, ListMultimap)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     *
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockExternalMetadataInvocation {
        private final String URL;
        private final ListMultimap<String, String> metadata;
    }
    
    @Getter private final List<MockExternalMetadataInvocation> invocations = new ArrayList<>();
    
    @Getter private final List<String> addExternalMetadataLineInvocations = new ArrayList<>();
    
    @Override
    public synchronized void addMetadataToPrefix(String URL, ListMultimap<String, String> metadata) throws IllegalArgumentException {
        this.invocations.add(new MockExternalMetadataInvocation(URL, metadata));
    }

    @Override
    public synchronized void addExternalMetadataLine(String externalMetadataLine) throws IllegalArgumentException {
        this.addExternalMetadataLineInvocations.add(externalMetadataLine);
    }

}
