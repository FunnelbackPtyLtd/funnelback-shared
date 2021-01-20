package com.funnelback.plugin.index.consumers.mock;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.KillByPartialMatchConsumer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A mock {@link KillByPartialMatchConsumer} that may be used when testing {@link IndexingConfigProvider#killByPartialMatch(IndexConfigProviderContext, KillByPartialMatchConsumer)}.
 * 
 * Example:
 * <pre>{@code 
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockKillByPartialMatchConsumer mockConsumer = new MockKillByPartialMatchConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.killByPartialMatch(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * }</pre>
 */
public class MockKillByPartialMatchConsumer implements KillByPartialMatchConsumer {

    /**
     * Holds the values that {@link MockKillByPartialMatchConsumer#killByPartialMatch(String)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     *
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MockKillByPartialMatchInvocation {
        private String urlToKillByPartialMatch;
    }
    
    /**
     * Hold the values of all invocations of {@link MockKillByPartialMatchConsumer#killByPartialMatch(String)}.
     * 
     */
    @Getter private final List<MockKillByPartialMatchInvocation> invocations = new ArrayList<>();
    
    @Override
    public synchronized void killByPartialMatch(String urlToKillByPartialMatch) throws IllegalArgumentException {
        this.invocations.add(new MockKillByPartialMatchInvocation(urlToKillByPartialMatch));
    }

}
