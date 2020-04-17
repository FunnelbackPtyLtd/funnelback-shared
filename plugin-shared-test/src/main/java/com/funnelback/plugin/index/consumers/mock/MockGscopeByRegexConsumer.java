package com.funnelback.plugin.index.consumers.mock;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.GscopeByRegexConsumer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A mock {@link GscopeByRegexConsumer} that may be used when testing {@link IndexingConfigProvider#supplyGscopesByRegex(IndexConfigProviderContext, GscopeByRegexConsumer)}.
 * 
 * Example:
 * <code>
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockGscopeByRegexConsumer mockConsumer = new MockGscopeByRegexConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.supplyGscopesByRegex(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * </code>
 */
public class MockGscopeByRegexConsumer implements GscopeByRegexConsumer {

    /**
     * Holds the values that {@link MockGscopeByRegexConsumer#applyGscopeWhenRegexMatches(String, String)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     * 
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockGscopeByRegexInvocation {
        private final String gscopeName;
        private final String query;
    }
    
    /**
     * Hold the values of all invocations of {@link MockGscopeByRegexConsumer#applyGscopeWhenRegexMatches(String, String)}.
     * 
     */
    @Getter private final List<MockGscopeByRegexInvocation> invocations = new ArrayList<>();
    
    @Override
    public synchronized void applyGscopeWhenRegexMatches(String gscopeName, String query) throws IllegalArgumentException {
        this.invocations.add(new MockGscopeByRegexInvocation(gscopeName, query));
    }

}
