package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Handles per-collection static resources
 */
@Controller
@Log4j2
public class ResourcesController implements ApplicationContextAware, ServletContextAware {

    /** Root path to use to access resources */
    public static final String MAPPING_PATH = "/resources/";
    
    private static final Pattern INVALID_PATH_PATTERN = Pattern.compile("(\\.\\.|/|\\\\|:)");
    
    /** Needed to instantiate Spring resource request handler */
    @Setter private ApplicationContext applicationContext;
    
    @Autowired
    private ConfigRepository configRepository;
    
    @Value("#{appProperties['resources.web.directory.name']?:\"web\"}")
    @Setter private String collectionWebResourcesDirectoryName;
    
    @Setter private String contextPath;
    
    /**
     * Handles a resource request
     * @param collectionId ID of the collection to load the resource from
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException 
     */
    @RequestMapping(MAPPING_PATH+"{collectionId}/**")
    public void handleRequest(@PathVariable String collectionId,
            HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Collection c = configRepository.getCollection(collectionId);
        
        if (c != null) {
            String prefix = contextPath + MAPPING_PATH + collectionId + "/";
            String resource = request.getRequestURI().substring(prefix.length());
        
            if (resource != null && ! "".equals(resource)) {
                // Extract profileId. The first part of the resource is either the profileId
                // or the actual file, i.e.
                // /resources/my-collection/_default_preview/folder/file.ext
                // /resources/my-collection/folder/file.ext    -> Should fall back to _default
                
                String profileId = DefaultValues.DEFAULT_PROFILE;
                if (resource.indexOf('/') > -1) {
                    String profileInUri = resource.substring(0, resource.indexOf('/'));
                    // Is this profile really existing ?
                    if (c.getProfiles().containsKey(profileInUri)) {
                        profileId = profileInUri;
                        resource = resource.substring(("/"+profileInUri).length());
                    }
                }
                
                String logicPath = "/" + collectionId + "/" + profileId + "/" + resource;
                
                log.debug("Processing request for resource '"+logicPath+"'");
        
                if (validateResourcePath(resource)) {
                    try {
                        FileSystemResource r = new FileSystemResource(
                                new File(c.getConfiguration().getConfigDirectory()
                                        + File.separator + profileId
                                        + File.separator + collectionWebResourcesDirectoryName)
                                    .getAbsolutePath() + File.separator);
                        log.trace("Looking up resource from " + r.getPath());
                        
                        List<Resource> locations = new ArrayList<Resource>();
                        locations.add(r);
                        
                        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, resource);
                        
                        ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
                        handler.setApplicationContext(applicationContext);
                        handler.setLocations(locations);
                    
                        handler.handleRequest(request, response);
                        return;
                    } catch (IOException ioe) {
                        log.error("Error on resource '" + logicPath + "'", ioe);
                    }
                }
            }
        }
            
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private boolean validateResourcePath(String path) {
        return ! INVALID_PATH_PATTERN.matcher(path).matches();
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.contextPath = servletContext.getContextPath();
    }
    
}
