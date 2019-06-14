package com.funnelback.publicui.search.web.views;

import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.istack.Nullable;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A view which restores the pre-spring-5.1 support for jsonp output which we rely on for
 * search.json and similarItems/recommender.json.
 *
 * See https://github.com/spring-projects/spring-framework/commit/ac37b678a3ac9ee541a10e8ad74d612bb9ec5b88
 * where they removed support for it - Most of the actual logic is lifted from there.
 */
public class FunnelbackMappingJackson2JsonView extends MappingJackson2JsonView {
    /**
     * Default content type for JSONP: "application/javascript".
     */
    public static final String DEFAULT_JSONP_CONTENT_TYPE = "application/javascript";

    /**
     * Pattern for validating jsonp callback parameter values.
     */
    private static final Pattern CALLBACK_PARAM_PATTERN = Pattern.compile("[0-9A-Za-z_\\.]*");

    @Nullable
    private Set<String> jsonpParameterNames = new LinkedHashSet<>(Arrays.asList("jsonp", "callback"));

    /**
     * Set JSONP request parameter names. Each time a request has one of those
     * parameters, the resulting JSON will be wrapped into a function named as
     * specified by the JSONP request parameter value.
     * <p>The parameter names configured by default are "jsonp" and "callback".
     * @since 4.1
     * @see <a href="http://en.wikipedia.org/wiki/JSONP">JSONP Wikipedia article</a>
     */
    public void setJsonpParameterNames(Set<String> jsonpParameterNames) {
        this.jsonpParameterNames = jsonpParameterNames;
    }

    @Nullable
    private String getJsonpParameterValue(HttpServletRequest request) {
        if (this.jsonpParameterNames != null) {
            for (String name : this.jsonpParameterNames) {
                String value = request.getParameter(name);
                if (StringUtils.isEmpty(value)) {
                    continue;
                }
                if (!isValidJsonpQueryParam(value)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ignoring invalid jsonp parameter value: " + value);
                    }
                    continue;
                }
                return value;
            }
        }
        return null;
    }

    /**
     * Validate the jsonp query parameter value. The default implementation
     * returns true if it consists of digits, letters, or "_" and ".".
     * Invalid parameter values are ignored.
     * @param value the query param value, never {@code null}
     * @since 4.1.8
     */
    protected boolean isValidJsonpQueryParam(String value) {
        return CALLBACK_PARAM_PATTERN.matcher(value).matches();
    }

    @Override
    protected Object filterAndWrapModel(Map<String, Object> model, HttpServletRequest request) {
        Object value = super.filterAndWrapModel(model, request);
        String jsonpParameterValue = getJsonpParameterValue(request);
        if (jsonpParameterValue != null) {
            if (value instanceof MappingJacksonValue) {
                ((FunnelbackMappingJacksonValue) value).setJsonpFunction(jsonpParameterValue);
            }
            else {
                FunnelbackMappingJacksonValue container = new FunnelbackMappingJacksonValue(value);
                container.setJsonpFunction(jsonpParameterValue);
                value = container;
            }
        }
        return value;
    }

    @Override
    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
        super.writePrefix(generator, object);

        String jsonpFunction = null;
        if (object instanceof FunnelbackMappingJacksonValue) {
            jsonpFunction = ((FunnelbackMappingJacksonValue) object).getJsonpFunction();
        }
        if (jsonpFunction != null) {
            generator.writeRaw("/**/");
            generator.writeRaw(jsonpFunction + "(");
        }
    }

    @Override
    protected void writeSuffix(JsonGenerator generator, Object object) throws IOException {
        String jsonpFunction = null;
        if (object instanceof FunnelbackMappingJacksonValue) {
            jsonpFunction = ((FunnelbackMappingJacksonValue) object).getJsonpFunction();
        }
        if (jsonpFunction != null) {
            generator.writeRaw(");");
        }
    }

    @Override
    protected void setResponseContentType(HttpServletRequest request, HttpServletResponse response) {
        if (getJsonpParameterValue(request) != null) {
            response.setContentType(DEFAULT_JSONP_CONTENT_TYPE);
        }
        else {
            super.setResponseContentType(request, response);
        }
    }
}
