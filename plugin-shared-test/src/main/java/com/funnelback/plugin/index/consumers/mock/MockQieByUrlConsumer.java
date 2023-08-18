package com.funnelback.plugin.index.consumers.mock;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.QieByQueryConsumer;
import com.funnelback.plugin.index.consumers.QieByUrlConsumer;
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
class MockQieByUrlConsumer implements QieByUrlConsumer {

    /**
     * Holds the values that {@link MockQieByUrlConsumer#applyQieWhenUrlMatches(double, String)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     * 
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockQieByQueryInvocation {
        private final double qieWeight;
        private final String url;
    }

    /**
     * Hold the values of all invocations of {@link MockQieByUrlConsumer#applyQieWhenUrlMatches(double, String)} .
     * 
     */
    @Getter private final List<MockQieByQueryInvocation> invocations = new ArrayList<>();
    
    @Override
    public synchronized void applyQieWhenUrlMatches(double qieWeight, String url) throws IllegalArgumentException {
        this.invocations.add(new MockQieByQueryInvocation(qieWeight, url));
    }


}
