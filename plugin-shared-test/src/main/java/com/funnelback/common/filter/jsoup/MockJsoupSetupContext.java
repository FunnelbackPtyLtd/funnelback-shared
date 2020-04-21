package com.funnelback.common.filter.jsoup;

import java.io.File;

import com.funnelback.mock.helpers.ConfigSettingMock;
import com.funnelback.mock.helpers.MapBackedConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * This can be used when testing Jsoup filters that need a {@link SetupContext}.
 * 
 * This supports setting the search home, the collection name as well as mocking
 * setting keys in collection.cfg.
 * 
 * Example:
 * <code>
 * MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
 * // Simulate setting keys in collection.cfg
 * setupContext.getConfigSettings().put(&#x22;myfilter.enable.this&#x22;, &#x22;true&#x22;);
 * setupContext.getConfigSettings().put(&#x22;myfilter.enable.that&#x22;, &#x22;false&#x22;);
 * </code>
 * 
 *
 */
public class MockJsoupSetupContext implements SetupContext, ConfigSettingMock {

    @Getter @Setter private File searchHome;

    @Getter @Setter private String collectionName;

    @Delegate private final MapBackedConfig mapBackedConfig = new MapBackedConfig();

    @Override
    public File getCollectionConfigFile(String filename) {
        throw new RuntimeException("Not mocked.");
    }

    
}
