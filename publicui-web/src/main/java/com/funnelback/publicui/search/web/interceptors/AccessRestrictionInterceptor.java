package com.funnelback.publicui.search.web.interceptors;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.net.NetUtils;
import com.funnelback.common.profile.ProfileAndView;
import com.funnelback.common.profile.ProfileId;
import com.funnelback.common.profile.ProfileView;
import com.funnelback.config.configtypes.mix.ProfileAndCollectionConfigOption;
import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.config.data.file.profile.FileProfileConfigData;
import com.funnelback.config.marshallers.Marshallers;
import com.funnelback.config.validators.Validators;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Checks access restriction at a collection level and either grant access,
 * deny access, or redirects user to an alternate collection.
 */
@Log4j2
public class AccessRestrictionInterceptor implements HandlerInterceptor {

    /**
     * A regex to check if the supplied IP range is in the old, unsupported, format
     */
    @Getter
    private final static Pattern OLD_IP_PATTERN = Pattern.compile("^[\\d\\.]+$");
    
    
    @Autowired
    private File searchHome;

    /**
     * Pattern to match collection id in the query string
     */
    private final static Pattern QUERY_STRING_COLLECTION_PATTERN = Pattern.compile(".*collection=([^&$]*)?.*");

    /**
     * Pattern to match the profile ID and view in the query string
     */
    private final static Pattern QUERY_STRING_PROFILE_AND_VIEW_PATTERN = Pattern.compile(".*profile=([^&$]*)?.*");

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
        if (request.getParameter(RequestParameters.COLLECTION) != null
            && request.getParameter(RequestParameters.COLLECTION).matches(Collection.COLLECTION_ID_PATTERN)) {

            if (request.getParameter(RequestParameters.PROFILE) != null
                //&& ProfileId.isValidname(request.getParameter(RequestParameters.PROFILE))
                ) {

            }
        }
        
        if (request.getParameter(RequestParameters.COLLECTION) != null
                && request.getParameter(RequestParameters.COLLECTION).matches(Collection.COLLECTION_ID_PATTERN)) {

            ServiceConfig serviceConfig = configRepository.getServiceConfig(request.getParameter(RequestParameters.COLLECTION), request.getParameter(RequestParameters.PROFILE));
            
            String accessRestriction = serviceConfig.get(new ProfileAndCollectionConfigOption<String>(
                Keys.ACCESS_RESTRICTION,
                Marshallers.STRING_MARSHALLER,
                Validators.acceptAll(),
                ""
                ));
            
            System.out.println("access_restriction="+accessRestriction);

            
            Collection c = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
            if (c != null) {
                if (c.getConfiguration().hasValue(Keys.ACCESS_RESTRICTION)) {
                    String accessRestriction2 = c.getConfiguration().value(Keys.ACCESS_RESTRICTION);
                    log.trace(Keys.ACCESS_RESTRICTION + " = '" + accessRestriction2 + "' for collection '" + c.getId() + "'");
                    if (DefaultValues.NO_RESTRICTION.equals(accessRestriction)) {
                        log.debug("Access restriction explicitely disabled. Granting access to " + c.getId());
                        return true;
                    } else if (DefaultValues.NO_ACCESS.equals(accessRestriction)) {
                        log.debug("Access restriction expliciltely set to " + DefaultValues.NO_ACCESS + ". Denying access");
                        denyAccess(request, response, c);
                        return false;
                    } else {
                        String ip = getConnectingIp(request, c);
                        String hostName = request.getRemoteHost();
                        
                        String[] authorized = StringUtils.split(accessRestriction, ",");
                        for (String range : authorized) {
                            if (NetUtils.isCIDR(range)) {
                                if (NetUtils.isIPv4AddressinCIDR(ip, range)){
                                    return true;
                                }
                            }else if (OLD_IP_PATTERN.matcher(range).matches()) {
                                //Catch IPs that don't have a slash, ie someone has entered a IP range  in the old 
                                //unsupported format
                                denyAccess(request, response, c, Keys.ACCESS_RESTRICTION + 
                                        " in this collection's collection.cfg is misconfigured, IP ranges must be in CIDR format");
                                return false;
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
     * @param message Message to be displayed.
     * @throws IOException
     */
    private void denyAccess(HttpServletRequest request, HttpServletResponse response, Collection collection, String message) throws IOException {
        if (collection.getConfiguration().hasValue(Keys.ACCESS_ALTERNATE)) {
            StringBuffer out = new StringBuffer(request.getContextPath()).append(request.getPathInfo());
            
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
            response.setContentType("text/plain");
            response.getWriter().write(i18n.tr("access.collection.denied", collection.getId(), message));
        }
    }
    
    private void denyAccess(HttpServletRequest request, HttpServletResponse response, Collection collection) throws IOException {
        denyAccess(request, response, collection, "");
    }
    
    public String getConnectingIp(HttpServletRequest request, Collection c) {
        String ip = request.getRemoteAddr();
        log.trace("Real request IP (ignoring X-Forwarded-For): " + ip);
        if (c.getConfiguration().valueAsBoolean(Keys.AccessRestriction.PREFER_X_FORWARDED_FOR)){
            ip = NetUtils.getIpPreferingXForwardedFor(ip
                    , request.getHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR)
                    , c.getConfiguration().value(Keys.AccessRestriction.IGNORED_IP_RANGES));
        }
        log.trace("Connecting IP is: " + ip);
        return ip;
    }

}
