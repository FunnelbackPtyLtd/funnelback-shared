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

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.DelegatingFilterProxy;

import waffle.servlet.NegotiateSecurityFilter;

/**
 * <p>Wrapper around the WAFFLE filter so that it can be disabled
 * easily in the publicui.properties file, instead of having to
 * edit <code>web.xml</code>.</p>
 * 
 * <p>I was forced to write a wrapper for {@link FilterConfig} to remove
 * the <em>targetFilterLifecycle</em> <code>&lt;init-param&gt;</code>. This param is a
 * Spring-specific parameter (As we use the {@link DelegatingFilterProxy} facility) and
 * it's passed along to WAFFLE, which don't recognize it and throws an exception.</p>
 */
@Log4j
public class ConfigurableSecurityFilter extends NegotiateSecurityFilter {

	@Value("#{appProperties['authentication']?:false}")
	@Setter private boolean active = false;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(new FilterConfigWrapper(config, new String[] {"targetFilterLifecycle"}));
		if (active) {
			log.info("Windows authentication filter is enabled");
		}
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		if (active) {
			super.doFilter(request, response, chain);
		} else {
			chain.doFilter(request, response);
		}
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
