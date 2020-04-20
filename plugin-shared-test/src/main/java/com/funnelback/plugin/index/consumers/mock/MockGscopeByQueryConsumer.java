package com.funnelback.plugin.index.consumers.mock;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.GscopeByQueryConsumer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A mock {@link GscopeByQueryConsumer} that may be used when testing {@link IndexingConfigProvider#supplyGscopesByQuery(IndexConfigProviderContext, GscopeByQueryConsumer)}.
 * 
 * Example:
 * <pre>{@code 
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockGscopeByQueryConsumer mockConsumer = new MockGscopeByQueryConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.supplyGscopesByQuery(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * }</pre>
 */
public class MockGscopeByQueryConsumer implements GscopeByQueryConsumer {

    /**
     * Holds the values that {@link MockGscopeByQueryConsumer#applyGscopeWhenQueryMatches(String, String)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     * 
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockGscopeByQueryInvocation {
        private final String gscopeName;
        private final String query;
    }
    
    /**
     * Hold the values of all invocations of {@link MockGscopeByQueryConsumer#applyGscopeWhenQueryMatches(String, String)}.
     * 
     */
    @Getter private final List<MockGscopeByQueryInvocation> invocations = new ArrayList<>();
    
    @Override
    public synchronized void applyGscopeWhenQueryMatches(String gscopeName, String query) throws IllegalArgumentException {
        this.invocations.add(new MockGscopeByQueryInvocation(gscopeName, query));
    }

}
