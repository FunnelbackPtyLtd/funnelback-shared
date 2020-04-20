package com.funnelback.plugin.index.consumers.mock;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.KillByPartialMatchConsumer;
import com.funnelback.plugin.index.consumers.MetadataMappingConsumer;
import com.funnelback.plugin.index.model.metadatamapping.MetadataSourceType;
import com.funnelback.plugin.index.model.metadatamapping.MetadataType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A mock {@link KillByPartialMatchConsumer} that may be used when testing {@link IndexingConfigProvider#metadataMappings(IndexConfigProviderContext, MetadataMappingConsumer)}.
 * 
 * Example:
 * <pre>{@code 
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockMetadataMappingConsumer mockConsumer = new MockMetadataMappingConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.metadataMappings(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * }</pre>
 *
 */
public class MockMetadataMappingConsumer implements MetadataMappingConsumer {

    /**
     * Holds the values that {@link MockMetadataMappingConsumer#map(String, MetadataType, MetadataSourceType, String)} was called with.
     *
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockMetadataMappingInvocation {
        private final String metadataClass;
        private final MetadataType type;
        private final MetadataSourceType sourceType;
        private final String locator;
    }
    
    /**
     * Hold the values of all invocations of {@link MockMetadataMappingConsumer#map(String, MetadataType, MetadataSourceType, String)}.
     * 
     */
    @Getter private final List<MockMetadataMappingInvocation> invocations = new ArrayList<>();
    
    @Override
    public void map(String metadataClass, MetadataType type, MetadataSourceType sourceType, String locator)
        throws IllegalArgumentException {
        this.invocations.add(new MockMetadataMappingInvocation(metadataClass, type, sourceType, locator));
    }

}
