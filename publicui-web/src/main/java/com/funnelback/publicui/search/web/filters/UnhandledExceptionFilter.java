package com.funnelback.publicui.search.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

/**
 * <p>Filter to handle exception that bubble up, to prevent them from bubbling up
 * further and ending up in the Jetty log.</p>
 * 
 * <p>This is useful to catch errors that
 * happens after a controller has run, such as errors in the view layer
 * (FreeMarker).</p>
 * 
 * @author nguillaumin@funnelback.com
 */
@Log4j2
public class UnhandledExceptionFilter implements Filter {

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            sendUnhandledExceptionErrorResponse(request, response, e);
        }
    }

    public void sendUnhandledExceptionErrorResponse(ServletRequest request, ServletResponse response, Exception e) {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        HttpServletRequest req = (HttpServletRequest) request;
        log.error("Unhandled exception for URL '{}?{}'", req.getRequestURL(), req.getQueryString(), e);
    }

    @Override
    public void init(FilterConfig config) throws ServletException { }

}
