package com.funnelback.plugin.index.consumers.mock;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.KillByExactMatchConsumer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A mock {@link KillByExactMatchConsumer} that may be used when testing {@link IndexingConfigProvider#killByExactMatch(IndexConfigProviderContext, KillByExactMatchConsumer)}.
 * 
 * Example:
 * <code>
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockKillByExactMatchConsumer mockConsumer = new MockKillByExactMatchConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.killByExactMatch(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * </code>
 */
public class MockKillByExactMatchConsumer implements KillByExactMatchConsumer {

    /**
     * Holds the values that {@link MockKillByExactMatchConsumer#killByExactMatch(String)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     *
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockKillByExactMatchInvocation {
        private String urlToKillByExactMatch;
    }
    
    /**
     * Hold the values of all invocations of {@link MockKillByExactMatchConsumer#killByExactMatch(String)}.
     * 
     */
    @Getter private final List<MockKillByExactMatchInvocation> invocations = new ArrayList<>();
    
    @Override
    public synchronized void killByExactMatch(String urlToKillByExactMatch) throws IllegalArgumentException {
        this.invocations.add(new MockKillByExactMatchInvocation(urlToKillByExactMatch));
    }

}
