package com.funnelback.filter.api.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.funnelback.filter.api.DocumentTypeFactory;
import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterDocumentFactory;

import lombok.Getter;
import lombok.Setter;

/**
 * A Filter context suitable for testing.
 *
 */
public class MockFilterContext implements FilterContext {

    @Getter @Setter private String collectionName;
    
    private final Map<String, String> config;
    
    @Getter @Setter private FilterDocumentFactory filterDocumentFactory = new MockFilterDocumentFactory();
    
    @Getter @Setter private DocumentTypeFactory documentTypeFactory = new UnknownDocumentTypeFactory();
    
    public MockFilterContext() {
        collectionName = "dummy-filtering-mock-collection-"+System.currentTimeMillis();
        config = new HashMap<>();
    }

    @Override
    public Set<String> getConfigKeys() {
        return config.keySet();
    }

    @Override
    public Optional<String> getConfigValue(String key) {
        return Optional.ofNullable(config.get(key));
    }
    
    public void setConfigValue(String key, String value) {
        config.put(key, value);
    }
    
    public static MockFilterContext getEmptyContext() {
        return new MockFilterContext();
    }
}
