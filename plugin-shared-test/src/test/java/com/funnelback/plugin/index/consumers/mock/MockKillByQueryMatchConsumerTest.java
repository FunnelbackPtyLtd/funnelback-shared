package com.funnelback.plugin.index.consumers.mock;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.KillByQueryMatchConsumer;
import com.funnelback.plugin.index.mock.MockIndexConfigProviderContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MockKillByQueryMatchConsumerTest {

    private static final String testQuery = "test";

    @Test
    public void test() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        MockKillByQueryMatchConsumer mockConsumer = new MockKillByQueryMatchConsumer();
        MockKillByQueryMatchConsumerTest.ValidPluginIndexingConfigProvider underTest = new MockKillByQueryMatchConsumerTest.ValidPluginIndexingConfigProvider();

        underTest.killByQueryMatch(mockContext, mockConsumer);

        Assertions.assertEquals(1, mockConsumer.getInvocations().size());
        Assertions.assertEquals(new MockKillByQueryMatchConsumer.MockKillByQueryMatchInvocation(
             testQuery
        ), mockConsumer.getInvocations().get(0));
    }


    private static class ValidPluginIndexingConfigProvider implements IndexingConfigProvider {
        @Override
        public void killByQueryMatch(IndexConfigProviderContext context, KillByQueryMatchConsumer consumer) {
            consumer.killByQueryMatch(testQuery);
        }
    }

}