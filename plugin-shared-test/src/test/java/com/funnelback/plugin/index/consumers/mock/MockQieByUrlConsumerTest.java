package com.funnelback.plugin.index.consumers.mock;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.QieByUrlConsumer;
import com.funnelback.plugin.index.mock.MockIndexConfigProviderContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MockQieByUrlConsumerTest {

    private static final double valid_qieWeight = 0.3;
    private static final double Invalid_qieWeight_greater_than_one = 1.3;
    private static final double Invalid_qieWeight_smaller_than_zero = -0.3;
    private static final String testURL = "http://www.example.com";

    private MockIndexConfigProviderContext mockContext;
    private MockQieByUrlConsumer mockConsumer;

    @BeforeEach
    public void setup() {
        mockContext = new MockIndexConfigProviderContext();
        mockConsumer = new MockQieByUrlConsumer();
    }

    @Test
    public void testValidInput() {
        MockQieByUrlConsumerTest.ExamplePluginIndexingConfigProvider underTest = new MockQieByUrlConsumerTest.ExamplePluginIndexingConfigProvider();

        underTest.supplyQieByURL(mockContext, mockConsumer);

        Assertions.assertEquals(1, mockConsumer.getInvocations().size());
        Assertions.assertEquals(new MockQieByUrlConsumer.MockQieByQueryInvocation(valid_qieWeight, testURL), mockConsumer.getInvocations().get(0));
    }

    @Test
    public void testQIEGreaterThanOne() {
        MockQieByUrlConsumerTest.QIEGreaterThanOneConfigProvider underTest = new MockQieByUrlConsumerTest.QIEGreaterThanOneConfigProvider();

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.supplyQieByURL(mockContext, mockConsumer));
        Assertions.assertEquals("Invalid QIE value: " + Invalid_qieWeight_greater_than_one + ". Its value shall be  0.0 - 1.0.", exception.getMessage());
    }

    @Test
    public void testQIESmallerThanZero() {
        MockQieByUrlConsumerTest.QIESmallerThanZeroConfigProvider underTest = new MockQieByUrlConsumerTest.QIESmallerThanZeroConfigProvider();

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.supplyQieByURL(mockContext, mockConsumer));
        Assertions.assertEquals("Invalid QIE value: " + Invalid_qieWeight_smaller_than_zero + ". Its value shall be  0.0 - 1.0.", exception.getMessage());
    }

    private static class ExamplePluginIndexingConfigProvider implements IndexingConfigProvider {
        @Override
        public void supplyQieByURL(IndexConfigProviderContext context, QieByUrlConsumer consumer) {
            consumer.applyQieWhenUrlMatches(valid_qieWeight,testURL);
        }
    }

    private static class QIEGreaterThanOneConfigProvider implements IndexingConfigProvider {
        @Override
        public void supplyQieByURL(IndexConfigProviderContext context, QieByUrlConsumer consumer) {
            consumer.applyQieWhenUrlMatches(Invalid_qieWeight_greater_than_one,testURL);
        }
    }

    private static class QIESmallerThanZeroConfigProvider implements IndexingConfigProvider {
        @Override
        public void supplyQieByURL(IndexConfigProviderContext context, QieByUrlConsumer consumer) {
            consumer.applyQieWhenUrlMatches(Invalid_qieWeight_smaller_than_zero,testURL);
        }
    }
}