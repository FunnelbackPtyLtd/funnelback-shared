package com.funnelback.publicui.search.web.views;

import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.lang.Nullable;

/**
 * This class wraps an object being serialised with information about the
 * jsonpFunction name it should be enclosed within when it is written out.
 *
 * See FunnelbackMappingJackson2JsonView for more details about why this
 * exists.
 */
public class FunnelbackMappingJacksonValue extends MappingJacksonValue {
    @Nullable
    private String jsonpFunction;

    public FunnelbackMappingJacksonValue(Object value) {
        super(value);
    }

    /**
     * Set the name of the JSONP function name.
     */
    public void setJsonpFunction(@Nullable String functionName) {
        this.jsonpFunction = functionName;
    }

    /**
     * Return the configured JSONP function name.
     */
    @Nullable
    public String getJsonpFunction() {
        return this.jsonpFunction;
    }
}
