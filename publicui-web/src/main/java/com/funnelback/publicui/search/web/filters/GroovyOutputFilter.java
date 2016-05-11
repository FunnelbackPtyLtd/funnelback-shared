package com.funnelback.publicui.search.web.filters;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.service.resource.impl.GroovyClassResource;
import com.funnelback.publicui.search.service.resource.impl.GroovyScriptResource;
import com.funnelback.publicui.search.web.filters.utils.CachingHttpServletResponseWrapper;
import com.funnelback.publicui.search.web.filters.utils.FilterParameterHandling;
import com.funnelback.springmvc.service.resource.ResourceManager;

import groovy.lang.Script;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * Filter to allow a groovy script to operate on the public ui output just
 * before it is returned.
 * </p>
 * 
 * <p>
 * Useful for recording information for auditing purposes, and perhaps for other
 * things down the road.
 * </p>
 * 
 * <p>
 * Note - Some code based off
 * http://www.oracle.com/technetwork/java/filters-137243.html
 * </p>
 * 
 * <p>
 * Note - Could possibly use AbstractRequestLoggingFilter instead (but I think a
 * generic hook point here might be useful)
 * </p>
 */
@Log4j2
public class GroovyOutputFilter implements Filter {

    private static final String OUTPUT_FILTER_CLASS_FILE_NAME = Files.HOOK_PREFIX + IGroovyOutputFilter.class.getSimpleName()
        + Files.HOOK_SUFFIX;

    @Autowired
    protected ResourceManager resourceManager;

    @Autowired
    protected File searchHome;

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            Optional<File> groovyOutputFilterClassFile = Optional.empty();

            // We could presumably support reading a profile level script here.
            //
            // Would need to consider whether to have per profile classloaders with their
            // own @groovy directories or not.

            String collectionId = new FilterParameterHandling().getCollectionId((HttpServletRequest) request);
            if (collectionId != null) {
                // Look for a collection level groovy script
                File configDirectory = new File(searchHome + File.separator + DefaultValues.FOLDER_CONF);
                File collectionConfigDirectory = new File(configDirectory, collectionId);
                if (new File(collectionConfigDirectory, OUTPUT_FILTER_CLASS_FILE_NAME).exists()) {
                    groovyOutputFilterClassFile = Optional.of(new File(collectionConfigDirectory, OUTPUT_FILTER_CLASS_FILE_NAME));
                }
            }

            // We could presumably support reading a server wide] script here.
            //
            // Would need to consider what classpaths the class loader would use in this case.

            if (groovyOutputFilterClassFile.isPresent()) {
                Class<IGroovyOutputFilter> clazz = resourceManager
                    .load(new GroovyClassResource<IGroovyOutputFilter>(groovyOutputFilterClassFile.get(), collectionId, searchHome));

                // We use this to detach the response from the user, caching
                // what's written so we can process it via the groovy script
                CachingHttpServletResponseWrapper wrappedResponse = new CachingHttpServletResponseWrapper((HttpServletResponse) response);

                chain.doFilter(request, wrappedResponse);

                // Run the groovy output filter
                byte[] originalBytes = wrappedResponse.getByteArray();
                Optional<byte[]> alteredBytes = Optional.empty();
                try {
                    alteredBytes = clazz.newInstance().filterResponse(request, originalBytes, wrappedResponse);
                } catch (InstantiationException e) {
                    throw new RuntimeException("TODO", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("TODO", e);
                }

                if (alteredBytes.isPresent()) {
                    wrappedResponse.setContentLength(alteredBytes.get().length);

                    // Write the response back out to the client
                    response.getOutputStream().write(alteredBytes.get());
                } else {
                    response.getOutputStream().write(originalBytes);                    
                }
            } else {
                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            HttpServletRequest req = (HttpServletRequest) request;
            log.error("Unhandled exception for URL '{}?{}'", req.getRequestURL(), req.getQueryString(), e);
        }

    }

    public void destroy() {
    }
}
