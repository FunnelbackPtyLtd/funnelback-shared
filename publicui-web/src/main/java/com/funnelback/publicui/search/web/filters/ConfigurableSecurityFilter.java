package com.funnelback.publicui.search.web.filters;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j;

import org.apache.commons.exec.OS;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.DelegatingFilterProxy;

import waffle.servlet.NegotiateSecurityFilter;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.controllers.ResourcesController;

/**
 * <p>Wrapper around the WAFFLE filter so that it can be disabled
 * easily in the publicui.properties file, instead of having to
 * edit <code>web.xml</code>.</p>
 * 
 * <p>I was forced to write a wrapper for {@link FilterConfig} to remove
 * the <em>targetFilterLifecycle</em> <code>&lt;init-param&gt;</code>. This param is a
 * Spring-specific parameter (As we use the {@link DelegatingFilterProxy} facility) and
 * it's passed along to WAFFLE, which doesn't recognize it and throws an exception.</p>
 */
@Log4j
public class ConfigurableSecurityFilter extends NegotiateSecurityFilter {

	@Autowired
	private ConfigRepository configRepository;
	
	private boolean active = false;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(new FilterConfigWrapper(config, new String[] {"targetFilterLifecycle"}));
	
		if (OS.isFamilyWindows()) {
			active = true;
			log.debug("Windows authentication filter is loaded. Use "+Keys.ModernUI.AUTHENTICATION+"=true to activate it on collections");
		}
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		if (active) {
			String collectionId = getCollectionId((HttpServletRequest) request);
			
			Collection collection = null;			
			if (collectionId != null) {
				collection = configRepository.getCollection(collectionId);
				if (collection != null && collection.getConfiguration().valueAsBoolean(Keys.ModernUI.AUTHENTICATION)) {
					log.debug("Using Windows authentication filter for collection '"+collectionId+"'");
					super.doFilter(request, response, chain);
					return;
				}
			}
		}
		
		chain.doFilter(request, response);
	}
	
	/**
	 * Look for the collection id in the query string as well
	 * as in the Path part of the URI for static requests like
	 * /resources/&lt;collection&gt;/&lt;profile&gt;/file.ext
	 * @param request
	 * @return
	 */
	private String getCollectionId(HttpServletRequest request) {
		String collectionId = request.getParameter(RequestParameters.COLLECTION);
		if (collectionId == null
				&& request.getPathInfo() != null
				&& request.getPathInfo().startsWith(ResourcesController.MAPPING_PATH)
				&& request.getPathInfo().indexOf('/', ResourcesController.MAPPING_PATH.length()) > -1) {
			collectionId = request.getPathInfo().substring(
					ResourcesController.MAPPING_PATH.length(),
					request.getPathInfo().indexOf('/', ResourcesController.MAPPING_PATH.length()));
		}
		
		return collectionId;
	}
		
	/**
	 * Wrapper around {@link FilterConfig} to allow customization
	 * of init parameters.
	 */
	public class FilterConfigWrapper implements FilterConfig {

		private String[] hiddenParamNames;
		private FilterConfig filterConfig;
		private Set<String> modifiedParameterNames = new HashSet<String>();
		
		public FilterConfigWrapper(FilterConfig filterConfig, String[] hiddenParamNames) {
			this.filterConfig = filterConfig;
			this.hiddenParamNames = hiddenParamNames;

			@SuppressWarnings("rawtypes")
			Enumeration parameterNames = filterConfig.getInitParameterNames();
			while(parameterNames.hasMoreElements()) {
				String name = (String) parameterNames.nextElement();
				if (! ArrayUtils.contains(hiddenParamNames, name)) {
					modifiedParameterNames.add(name);
				}
			}
				
		}
		
		@Override
		public String getFilterName() {
			return filterConfig.getFilterName();
		}

		@Override
		public String getInitParameter(String name) {
			if (ArrayUtils.contains(hiddenParamNames, name)) {
				return null;
			} else {
				return filterConfig.getInitParameter(name);
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Enumeration getInitParameterNames() {
			return Collections.enumeration(modifiedParameterNames);
		}

		@Override
		public ServletContext getServletContext() {
			return filterConfig.getServletContext();
		}
		
	}
	
}
