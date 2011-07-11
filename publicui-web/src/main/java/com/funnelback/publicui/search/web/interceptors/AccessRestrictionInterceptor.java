package com.funnelback.publicui.search.web.interceptors;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Checks access restriction at a collection level and either grant access,
 * deny access, or redirects user to an alternate collection.
 */
@Log
public class AccessRestrictionInterceptor implements HandlerInterceptor {

	/**
	 * Pattern to check if the restriction range is IP based.
	 * Colon for ipv6 addresses.
	 */
	private final static Pattern HOSTNAME_IP_PATTERN = Pattern.compile("^[\\d\\.\\:]+$");
	
	/**
	 * Pattern to match collection id in the query string
	 */
	private final static Pattern QUERY_STRING_COLLECTION_PATTERN = Pattern.compile(".*collection=(.*)?($|&)");
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Autowired
	private I18n i18n;
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception { }

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mav)
			throws Exception { }

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (request.getParameter(RequestParameters.COLLECTION) != null) {
			Collection c = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
			if (c != null) {
				if (c.getConfiguration().hasValue(Keys.ACCESS_RESTRICTION)) {
					String accessRestriction = c.getConfiguration().value(Keys.ACCESS_RESTRICTION);
					log.debug(Keys.ACCESS_RESTRICTION + " = '" + accessRestriction + "' for collection '" + c.getId() + "'");
					if (DefaultValues.NO_RESTRICTION.equals(accessRestriction)) {
						log.debug("Access restriction explicitely disabled. Granting access to " + c.getId());
						return true;
					} else if (DefaultValues.NO_ACCESS.equals(accessRestriction)) {
						log.debug("Access restriction expliciltely set to " + DefaultValues.NO_ACCESS + ". Denying access");
						denyAccess(request, response, c);
						return false;
					} else {
						String ip = request.getRemoteAddr();
						String hostName = request.getRemoteHost();
						
						String[] authorized = StringUtils.split(accessRestriction, ",");
						for (String range : authorized) {
							if (HOSTNAME_IP_PATTERN.matcher(range).matches()) {
								// It's an IP range
								if (ip.matches("^" + range + "(.*)$")) {
									log.debug("'" + ip + "' matches '" + range + "'. Granting access to '" + c.getId() + "'");
									return true;
								}
							} else {
								// It's a hostname
								if (hostName.matches("^.*" + range + "$")) {
									log.debug("'" + hostName + "' matches '" + range + "'. Granting access to '" + c.getId() + "'");
									return true;
								}
							}
						}
						log.debug("Neither IP '" + ip + "' or hostname '" + hostName + "' matched. Denying access to '" + c.getId() + "'");
						denyAccess(request, response, c);
						return false;
					}
				} else {
					log.debug("No " + Keys.ACCESS_RESTRICTION + " setting for collection '" + c.getId() + "'");
				}
			} else {
				log.debug("Invalid collection id '" + request.getParameter(RequestParameters.COLLECTION) + "'");
			}
		}
		return true;
	}
	
	/**
	 * Denies access by either returning a 403, or redirecting to an alternate collection.
	 * @param request
	 * @param response
	 * @param collection
	 * @throws IOException
	 */
	private void denyAccess(HttpServletRequest request, HttpServletResponse response, Collection collection) throws IOException {
		if (collection.getConfiguration().hasValue(Keys.ACCESS_ALTERNATE)) {
			StringBuffer out = request.getRequestURL();
			
			Matcher m = QUERY_STRING_COLLECTION_PATTERN.matcher(request.getQueryString());
			if (m.find()) {
				out.append("?")
					.append(request.getQueryString().substring(0, m.start(1)))
					.append(collection.getConfiguration().value(Keys.ACCESS_ALTERNATE))
					.append(request.getQueryString().substring(m.end(1), request.getQueryString().length()));
			} else {
				// No collection in the initial request, should not happen as we should have been
				// unable to check ACCESS_RESTRICTION at the first place
				throw new IllegalStateException(i18n.tr("parameter.missing", RequestParameters.COLLECTION));
			}
			
			log.debug("Applying access alternate setting for collection '" + collection.getId() + "'. Redirecting to '" + out.toString() + "'");
			response.sendRedirect(out.toString());
		} else {
			// No access_alternate. Simply deny access
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}

}
