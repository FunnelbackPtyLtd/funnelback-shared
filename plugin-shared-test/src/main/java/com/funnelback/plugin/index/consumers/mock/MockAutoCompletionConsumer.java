package com.funnelback.plugin.index.consumers.mock;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.AutoCompletionConsumer;
import com.funnelback.plugin.index.consumers.ExternalMetadataConsumer;
import com.funnelback.plugin.index.model.querycompletion.AutoCompletionEntry;
import com.google.common.collect.ListMultimap;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A mock {@link com.funnelback.plugin.index.consumers.AutoCompletionConsumer} that may be used when testing
 * {@link IndexingConfigProvider#supplyAutoCompletionEntriesForProfiles(List, AutoCompletionConsumer)}.
 * 
 * Example:
 * <pre>{@code 
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockAutoCompletionConsumer mockConsumer = new MockAutoCompletionConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.supplyAutoCompletionEntriesForProfiles(List.of(mockContext), mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * }</pre>
 */
public class MockAutoCompletionConsumer implements AutoCompletionConsumer {

    /**
     * Holds the values that {@link MockAutoCompletionConsumer#applyAutoCompletionEntryToProfiles(AutoCompletionEntry, Set<String>)}
     * was called with.
     * 
     * Although this class is immutable, the fields are not.
     *
     */
    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    @Getter
    public static class MockAutoCompletionInvocation {
        private final AutoCompletionEntry entry;
        private final Set<String> profiles;
    }
    
    @Getter private final List<MockAutoCompletionInvocation> invocations = new ArrayList<>();
    
    @Override
    public synchronized void applyAutoCompletionEntryToProfiles(AutoCompletionEntry autoCompletionEntry, Set<String> profiles) throws IllegalArgumentException {
        this.invocations.add(new MockAutoCompletionInvocation(autoCompletionEntry, profiles));
    }

}
