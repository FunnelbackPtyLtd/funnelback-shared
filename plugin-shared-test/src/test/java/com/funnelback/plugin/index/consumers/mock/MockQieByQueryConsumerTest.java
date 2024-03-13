package com.funnelback.plugin.index.consumers.mock;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.QieByQueryConsumer;
import com.funnelback.plugin.index.mock.MockIndexConfigProviderContext;
import org.junit.Assert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MockQieByQueryConsumerTest {

    private static final double valid_qieWeight = 0.3;
    private static final double Invalid_qieWeight_greater_than_one = 1.3;

    private static final double Invalid_qieWeight_smaller_than_zero = -0.3;

    private static final String testQuery = "test";

    private MockIndexConfigProviderContext mockContext;

    private MockQieByQueryConsumer mockConsumer;

    @BeforeEach
    public void setup() {
        mockContext = new MockIndexConfigProviderContext();
        mockConsumer = new MockQieByQueryConsumer();
    }

    @Test
    public void testValidInput() {

        MockQieByQueryConsumerTest.ValidPluginIndexingConfigProvider underTest = new MockQieByQueryConsumerTest.ValidPluginIndexingConfigProvider();

        underTest.supplyQieByQuery(mockContext, mockConsumer);

        Assertions.assertEquals(1, mockConsumer.getInvocations().size());
        Assertions.assertEquals(new MockQieByQueryConsumer.MockQieByQueryInvocation(
                valid_qieWeight, testQuery
        ), mockConsumer.getInvocations().get(0));
    }

    @Test
    public void testQIEGreaterThanOne() {

        MockQieByQueryConsumerTest.QIEGreaterThanOneConfigProvider underTest = new MockQieByQueryConsumerTest.QIEGreaterThanOneConfigProvider();

        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> underTest.supplyQieByQuery(mockContext, mockConsumer));
        Assertions.assertEquals("Invalid QIE value: " + Invalid_qieWeight_greater_than_one + ". Its value shall be  0.0 - 1.0.", exception.getMessage());

    }

    @Test
    public void testQIESmallerThanZero() {

        MockQieByQueryConsumerTest.QIESmallerThanZeroConfigProvider underTest = new MockQieByQueryConsumerTest.QIESmallerThanZeroConfigProvider();

        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> underTest.supplyQieByQuery(mockContext, mockConsumer));
        Assertions.assertEquals("Invalid QIE value: " + Invalid_qieWeight_smaller_than_zero + ". Its value shall be  0.0 - 1.0.", exception.getMessage());

    }

    private static class ValidPluginIndexingConfigProvider implements IndexingConfigProvider {
        @Override
        public void supplyQieByQuery(IndexConfigProviderContext context, QieByQueryConsumer consumer) {
            consumer.applyQieWhenQueryMatches(valid_qieWeight, testQuery);
        }
    }

    private static class QIEGreaterThanOneConfigProvider implements IndexingConfigProvider {
        @Override
        public void supplyQieByQuery(IndexConfigProviderContext context, QieByQueryConsumer consumer) {
            consumer.applyQieWhenQueryMatches(Invalid_qieWeight_greater_than_one, testQuery);
        }
    }

    private static class QIESmallerThanZeroConfigProvider implements IndexingConfigProvider {
        @Override
        public void supplyQieByQuery(IndexConfigProviderContext context, QieByQueryConsumer consumer) {
            consumer.applyQieWhenQueryMatches(Invalid_qieWeight_smaller_than_zero, testQuery);
        }
    }
}