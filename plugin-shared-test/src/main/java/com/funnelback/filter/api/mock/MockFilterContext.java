package com.funnelback.filter.api.mock;


import java.util.Optional;

import java.io.File;

import com.funnelback.filter.api.DocumentTypeFactory;
import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterDocumentFactory;
import com.funnelback.mock.helpers.MapBackedConfig;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import lombok.experimental.Delegate;

/**
 * A Filter context suitable for testing.
 *
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockFilterContext implements FilterContext {

    @Getter @Setter private String collectionName;
    
    @Delegate private final MapBackedConfig mapBackedConfig = new MapBackedConfig();
    
    @Getter @Setter @With private FilterDocumentFactory filterDocumentFactory = new MockFilterDocumentFactory();
    
    @Getter @Setter @With private DocumentTypeFactory documentTypeFactory = new UnknownDocumentTypeFactory();
    
    public MockFilterContext() {
        collectionName = "dummy-filtering-mock-collection-"+System.currentTimeMillis();
    }
    
    @Override
    public Optional<String> getConfigValue(String key) {
        return Optional.ofNullable(mapBackedConfig.getConfigSetting(key));
    }

    @Override public File getCollectionConfigFile(String filename) {
        throw new RuntimeException("Not mocked.");
    }

    public void setConfigValue(String key, String value) {
        mapBackedConfig.setConfigSetting(key, value);
    }
    
    public static MockFilterContext getEmptyContext() {
        return new MockFilterContext();
    }
}
