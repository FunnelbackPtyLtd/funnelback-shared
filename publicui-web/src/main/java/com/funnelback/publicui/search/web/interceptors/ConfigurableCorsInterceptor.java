package com.funnelback.publicui.search.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Interceptor to add a CORS allow origin header
 * if configured on the collection
 * 
 * @author nguillaumin@funnelback.com
 */
public class ConfigurableCorsInterceptor implements HandlerInterceptor {

    @Autowired
    @Setter
    private ConfigRepository configRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (request.getParameter(RequestParameters.COLLECTION) != null
                && request.getParameter(RequestParameters.COLLECTION).matches(Collection.COLLECTION_ID_PATTERN)) {
            Collection c = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
            
            if (c.getConfiguration().hasValue(Keys.ModernUI.CORS_ALLOW_ORIGIN)) {
                response.addHeader("Access-Control-Allow-Origin", c.getConfiguration().value(Keys.ModernUI.CORS_ALLOW_ORIGIN));
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

}
