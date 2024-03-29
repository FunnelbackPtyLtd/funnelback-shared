package com.funnelback.plugin.index.consumers.mock;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.QieByQueryConsumer;
import com.funnelback.plugin.index.consumers.QieByUrlConsumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A mock {@link QieByQueryConsumer} that may be used when testing {@link IndexingConfigProvider#supplyQieByURL(IndexConfigProviderContext, QieByUrlConsumer)}.
 * 
 * Example:
 * <pre>{@code 
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * MockQieByUrlConsumer mockConsumer = new MockQieByUrlConsumer();
 * ExamplePluginIndexingConfigProvider underTest = new ExamplePluginIndexingConfigProvider();
 * 
 * underTest.supplyQieByUrl(mockContext, mockConsumer);
 * 
 * Assert.assertTrue("Assert something useful.", mockConsumer.getInvocations().size() >= 0);
 * }</pre>
 */
public class MockQieByUrlConsumer implements QieByUrlConsumer {

    /**
     * Holds the values that {@link MockQieByUrlConsumer#applyQieWhenUrlMatches(double, String)} was called with.
     * 
     * Although this class is immutable, the fields are not.
     * 
     */
    @EqualsAndHashCode
    @Getter
    public static class MockQieByQueryInvocation {
        private final double qieWeight;
        private final String url;

        MockQieByQueryInvocation(double qieWeight, String url) {
            if ( qieWeight > 1 || qieWeight < 0 ) {
                throw new IllegalArgumentException("Invalid QIE value: " + qieWeight + ". Its value shall be  0.0 - 1.0.");
            }

            this.qieWeight = qieWeight;
            this.url = url;
        }
    }

    /**
     * Hold the values of all invocations of {@link MockQieByUrlConsumer#applyQieWhenUrlMatches(double, String)} .
     * 
     */
    @Getter private final List<MockQieByQueryInvocation> invocations = new ArrayList<>();
    
    @Override
    public synchronized void applyQieWhenUrlMatches(double qieWeight, String url) throws IllegalArgumentException {
        this.invocations.add(new MockQieByQueryInvocation(qieWeight, url));
    }


}
