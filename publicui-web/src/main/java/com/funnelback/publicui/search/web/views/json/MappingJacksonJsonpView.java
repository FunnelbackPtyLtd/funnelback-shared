package com.funnelback.publicui.search.web.views.json;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * <p>Subclass of {@link MappingJacksonJsonView} that implements support
 * for <a href="http://en.wikipedia.org/wiki/JSONP">JSON-P</a> padding.</p>
 * 
 * <p>The callback name should be provided as a request parameter.</p>
 */
public class MappingJacksonJsonpView extends MappingJackson2JsonView {

    /** Pattern to validate the JS callback function name */
    private final static Pattern JS_FUNCTION_PATTERN = Pattern.compile("^[$A-Z_][0-9A-Z_$]*$", Pattern.CASE_INSENSITIVE);
    
    @Setter private String callbackParameterName = "callback";
    
    
    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        String callback = request.getParameter(callbackParameterName);
        if (isValidCallback(callback)) {
            response.getOutputStream().write(callback.getBytes());
            response.getOutputStream().write("(".getBytes());
        }
        
        super.renderMergedOutputModel(model, request, response);
        
        if (isValidCallback(callback)) {
            response.getOutputStream().write(")".getBytes());
        }
    }
    
    private boolean isValidCallback(String callback) {
        return callback != null && JS_FUNCTION_PATTERN.matcher(callback).matches();
    }
}
