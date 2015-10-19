package com.funnelback.publicui.search.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Filter to add a CORS allow origin header
 * if configured on the collection
 * 
 * @author nguillaumin@funnelback.com
 */
public class ConfigurableCorsFilter implements Filter {

    @Autowired
    @Setter
    private ConfigRepository configRepository;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (request.getParameter(RequestParameters.COLLECTION) != null
                && request.getParameter(RequestParameters.COLLECTION).matches(Collection.COLLECTION_ID_PATTERN)) {
            Collection c = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
            
            if (c.getConfiguration().hasValue(Keys.ModernUI.CORS_ALLOW_ORIGIN)) {
                ((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", c.getConfiguration().value(Keys.ModernUI.CORS_ALLOW_ORIGIN));
            }
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }

}
