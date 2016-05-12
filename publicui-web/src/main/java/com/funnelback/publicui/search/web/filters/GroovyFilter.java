package com.funnelback.publicui.search.web.filters;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.service.resource.impl.GroovyClassResource;
import com.funnelback.publicui.search.web.filters.utils.FilterParameterHandling;
import com.funnelback.springmvc.service.resource.ResourceManager;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * Filter to allow a groovy script to operate on the public ui input before it's
 * processed, and output just before it is returned.
 * </p>
 * 
 * <p>
 * Useful for recording information for auditing purposes, or programmatically
 * modifying Funnelback output before it is returned to the user.
 * </p>
 * 
 * <p>
 * Note: For modifying input parameters, a pre_process hook script should be
 * used in preference.
 * </p>
 */
@Log4j2
public class GroovyFilter implements Filter {

    public static final String OUTPUT_FILTER_CLASS_FILE_NAME = GroovyServletFilterHook.class.getSimpleName() + "PublicUIImpl"
        + Files.HOOK_SUFFIX;

    @Autowired
    @Setter
    protected ResourceManager resourceManager;

    @Autowired
    @Setter
    protected File searchHome;
    
    @Setter
    protected FilterParameterHandling filterParameterHandling = new FilterParameterHandling();

    @Autowired
    protected UnhandledExceptionFilter unhandledExceptionFilter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            Optional<File> groovyOutputFilterClassFile = Optional.empty();

            // We could presumably support reading a profile level script here.
            //
            // Would need to consider whether to have per profile classloaders
            // with their own @groovy directories or not.

            String collectionId = filterParameterHandling.getCollectionId((HttpServletRequest) request);
            if (collectionId != null) {
                // Look for a collection level groovy script
                File configDirectory = new File(searchHome + File.separator + DefaultValues.FOLDER_CONF);
                File collectionConfigDirectory = new File(configDirectory, collectionId);
                if (new File(collectionConfigDirectory, OUTPUT_FILTER_CLASS_FILE_NAME).exists()) {
                    groovyOutputFilterClassFile = Optional.of(new File(collectionConfigDirectory, OUTPUT_FILTER_CLASS_FILE_NAME));
                }
            }

            // We could presumably support reading a server wide script here.
            //
            // Would need to consider what classpaths the class loader would use
            // in this case.

            Class<GroovyServletFilterHook> groovyServletFilterHookClass = null;
            if (groovyOutputFilterClassFile.isPresent()) {
                groovyServletFilterHookClass = resourceManager
                    .load(new GroovyClassResource<GroovyServletFilterHook>(groovyOutputFilterClassFile.get(), collectionId, searchHome));
            }
            
            if (groovyServletFilterHookClass != null) {
                GroovyServletFilterHook groovyServletFilterHook = groovyServletFilterHookClass.newInstance();

                ServletResponse possiblyWrappedResponse = groovyServletFilterHook.preFilterResponse(request, response);

                try {
                    chain.doFilter(request, possiblyWrappedResponse);
                } finally {
                    groovyServletFilterHook.postFilterResponse(request, possiblyWrappedResponse);
                }
            } else {
                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            // We sit outside UnhandledExceptionFilter (so we can capture its
            // output for auditing, but we reuse its response if something fails
            // here).
            unhandledExceptionFilter.sendUnhandledExceptionErrorResponse(request, response, e);
        }

    }

    @Override
    public void destroy() {
    }
}
