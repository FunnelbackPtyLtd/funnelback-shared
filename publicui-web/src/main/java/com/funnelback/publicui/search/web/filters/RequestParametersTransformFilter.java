package com.funnelback.publicui.search.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Servlet {@link Filter} to perform query string parameters transformation.
 * Transformation rules are per-collection basis, in a specific config file
 * cgi_transform.cfg.
 *
 */
@Log
public class RequestParametersTransformFilter implements Filter {

	@Autowired
	private ConfigRepository configRepository;
	
	/**
	 * Never called as Spring creates the bean, not the servlet container.
	 * @see DelegatingFilterProxy
	 */
	@Override
	public void destroy() { }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		String collection = request.getParameter(RequestParameters.COLLECTION);
		ServletRequest target = request;
		if (collection != null) {
			Collection c = configRepository.getCollection(collection);
			if (c != null && c.getParametersTransforms() != null && c.getParametersTransforms().size() > 0) {
				log.debug("Using parameter transforms for collection '" + c.getId() + "'");
				target = new RequestParametersTransformWrapper((HttpServletRequest) request, c.getParametersTransforms());
			}
		}
		
		chain.doFilter(target, response);
	}

	/**
	 * Never called as Spring creates the bean, not the servlet container.
	 * @see DelegatingFilterProxy
	 */
	@Override
	public void init(FilterConfig config) throws ServletException { }

}
