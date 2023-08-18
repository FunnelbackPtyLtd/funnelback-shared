package com.funnelback.plugin.index.consumers.mock;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.QieByQueryConsumer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A mock {@link QieByQueryConsumer} that may be used when testing {@link IndexingConfigProvider#supplyQieByQuery(IndexConfigProviderContext, QieByQueryConsumer)}.
 * 
 * Example:
 * <pre>{@code 
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockQieByQueryConsumer mockConsumer = new MockQieByQueryConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.supplyQieByQuery(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * }</pre>
 */
class MockQieByQueryConsumer implements QieByQueryConsumer {

    /**
     * Holds the values that {@link MockQieByQueryConsumer#applyQieWhenQueryMatches(Double, String)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     * 
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockQieByQueryInvocation {
        private final double qieWeight;
        private final String query;
    }

    /**
     * Hold the values of all invocations of {@link MockGscopeByQueryConsumer#applyGscopeWhenQueryMatches(String, String)}.
     * 
     */
    @Getter private final List<MockQieByQueryInvocation> invocations = new ArrayList<>();
    
    @Override
    public synchronized void applyQieWhenQueryMatches(double qieWeight, String query) throws IllegalArgumentException {
        this.invocations.add(new MockQieByQueryInvocation(qieWeight, query));
    }


}
