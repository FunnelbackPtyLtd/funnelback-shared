package com.funnelback.plugin.index.mock;

import java.util.Optional;

import java.io.File;
import java.io.IOException;

import com.funnelback.mock.helpers.MapBackedConfig;
import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * A mock {@link IndexConfigProviderContext} that may be used when testig the {@link IndexingConfigProvider}.
 * 
 * Example:
 * <code>
 * MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
 * // Set collection or profile config setting 'foo=bar'.
 * mockContext.setConfigSetting("foo", "bar");
 * 
 * // This conext is for a profile and the method being tested needs to know the
 * // profile name, so set that with the mock.
 * mockContext.setProfile(Optional.of("dummy-profile"));
 * </code>
 *
 */
public class MockIndexConfigProviderContext implements IndexConfigProviderContext {

    @Getter @Setter private File searchHome;

    @Getter @Setter private String collectionName;

    @Getter @Setter @NonNull private Optional<String> profileWithView = Optional.empty();
    
    @Getter @Setter @NonNull private Optional<String> profile = Optional.empty();
        
    
    @Delegate private final MapBackedConfig mapBackedConfig = new MapBackedConfig();

    @Override
    public Optional<byte[]> readCollectionConfigFile(String... pathsBelowConf) throws IOException {
        this.setProfile(Optional.of("dummy-profile"));
        throw new RuntimeException("Not mocked.");
    }
}
