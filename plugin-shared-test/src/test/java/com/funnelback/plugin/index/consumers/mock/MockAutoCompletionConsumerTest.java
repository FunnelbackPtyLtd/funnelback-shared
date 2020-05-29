package com.funnelback.plugin.index.consumers.mock;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.AutoCompletionConsumer;
import com.funnelback.plugin.index.mock.MockIndexConfigProviderContext;
import com.funnelback.plugin.index.model.querycompletion.AutoCompletionEntry;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class MockAutoCompletionConsumerTest {

    @Test
    public void test() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        MockAutoCompletionConsumer mockConsumer = new MockAutoCompletionConsumer();
        ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();

        underTest.supplyAutoCompletionEntriesForProfiles(List.of(mockContext), mockConsumer);

        Assert.assertEquals(1, mockConsumer.getInvocations().size());
        Assert.assertEquals(new MockAutoCompletionConsumer.MockAutoCompletionInvocation(
            AutoCompletionEntry.builder().trigger("test").build(), Set.of("profile")
        ), mockConsumer.getInvocations().get(0));
    }

    private class ExamplePluginIndexingConfigProvider implements IndexingConfigProvider {
        @Override
        public void supplyAutoCompletionEntriesForProfiles(
            List<IndexConfigProviderContext> contextForProfilesThatRunThisPlugin,
            AutoCompletionConsumer consumer) {
            consumer.applyAutoCompletionEntryToProfiles(AutoCompletionEntry.builder().trigger("test").build(), Set.of("profile"));
        }
    }
}