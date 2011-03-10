package com.funnelback.publicui.web.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Handles per-collection static resources
 */
@Controller
@RequestMapping({"/resources"})
@lombok.extern.apachecommons.Log
public class ResourcesController implements ApplicationContextAware {

	private static final Pattern INVALID_PATH_PATTERN = Pattern.compile("(\\.\\.|/|\\\\|:)");
	
	/** Needed to instantiate Spring resource request handler */
	@Setter private ApplicationContext applicationContext;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Value("#{appProperties['resources.web.directory.name=web']}")
	private String collectionWebResourcesDirectoryName = "web";
	
	@RequestMapping("/{collectionId}/{resource:.*}")
	public void handleRequestDefaultProfile(@PathVariable String collectionId, @PathVariable String resource,
			HttpServletRequest request, HttpServletResponse response) throws ServletException {
		handleRequest(collectionId, DefaultValues.DEFAULT_PROFILE, resource, request, response);
	}
	
	@RequestMapping("/{collectionId}/{profileId}/{resource:.*}")
	public void handleRequest(@PathVariable String collectionId, @PathVariable String profileId, @PathVariable String resource,
			HttpServletRequest request, HttpServletResponse response) throws ServletException {
		Collection c = configRepository.getCollection(collectionId);
		
		String logicPath = "/" + collectionId + "/" + profileId + "/" + resource;
		if (c != null && c.getProfiles().get(profileId) != null) {
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
				
					ResourceHttpRequestHandler handler = applicationContext.getAutowireCapableBeanFactory().createBean(ResourceHttpRequestHandler.class);
					handler.setLocations(locations);
				
					handler.handleRequest(request, response);
				} catch (IOException ioe) {
					log.error("Error on resource '" + logicPath + "'", ioe); 
				}
			}
		}

		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	private boolean validateResourcePath(String path) {
		return ! INVALID_PATH_PATTERN.matcher(path).matches();
	}
	
}
