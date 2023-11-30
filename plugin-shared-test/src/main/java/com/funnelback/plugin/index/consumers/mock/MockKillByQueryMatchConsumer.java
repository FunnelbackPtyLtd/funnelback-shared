package com.funnelback.plugin.index.consumers.mock;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.KillByQueryMatchConsumer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A mock {@link KillByQueryMatchConsumer} that may be used when testing {@link IndexingConfigProvider#killByQueryMatch(IndexConfigProviderContext, KillByQueryMatchConsumer)}.
 * 
 * Example:
 * <pre>{@code 
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockKillByQueryMatchConsumer mockConsumer = new MockKillByQueryMatchConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.killByExactMatch(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * }</pre>
 */
public class MockKillByQueryMatchConsumer implements KillByQueryMatchConsumer {

    /**
     * Holds the values that {@link MockKillByQueryMatchConsumer#killByQueryMatch(String)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     *
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockKillByQueryMatchInvocation {
        private String urlToKillByQueryMatch;
    }
    
    /**
     * Hold the values of all invocations of {@link MockKillByQueryMatchConsumer#killByQueryMatch(String)}.
     * 
     */
    @Getter private final List<MockKillByQueryMatchInvocation> invocations = new ArrayList<>();

    @Override
    public synchronized void killByQueryMatch(String urlToKillByQueryMatch) throws IllegalArgumentException {
        this.invocations.add(new MockKillByQueryMatchInvocation(urlToKillByQueryMatch));
    }

}
