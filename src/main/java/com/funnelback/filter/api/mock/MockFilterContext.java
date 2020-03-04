package com.funnelback.filter.api.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.filter.api.DocumentTypeFactory;
import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterDocumentFactory;
import com.funnelback.filter.context.DefaultDocumentTypeFactory;
import com.funnelback.filter.context.DefaultFilterDocumentFactory;

public class MockFilterContext implements FilterContext {

    @Getter @Setter private String collectionName;
    private Map<String, String> map;
    
    @Getter private final FilterDocumentFactory filterDocumentFactory = new DefaultFilterDocumentFactory();
    
    @Getter private final DocumentTypeFactory documentTypeFactory = new DefaultDocumentTypeFactory();
    
    private MockFilterContext() {
        collectionName = "dummy-filtering-mock-collection-"+System.currentTimeMillis();
        map = new HashMap<>();
    }

    @Override
    public Set<String> getConfigKeys() {
        return map.keySet();
    }

    @Override
    public Optional<String> getConfigValue(String key) {
        return Optional.ofNullable(map.get(key));
    }
    
    public void setConfigValue(String key, String value) {
        map.put(key, value);
    }
    
    public static MockFilterContext getEmptyContext() {
        return new MockFilterContext();
    }
}
