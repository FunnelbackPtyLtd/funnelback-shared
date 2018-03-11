package com.funnelback.publicui.search.web.interceptors;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.net.NetUtils;
import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.exception.InvalidCollectionException;
import com.funnelback.publicui.search.web.interceptors.helpers.IntercepterHelper;
import com.funnelback.publicui.utils.web.ProfilePicker;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

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
    
    private IntercepterHelper intercepterHelper = new IntercepterHelper();

    /**
     * Pattern to match collection id in the query string
     */
    private final static Pattern QUERY_STRING_COLLECTION_PATTERN = Pattern.compile(".*collection=([^&$]*)?.*");

    /**
     * Pattern to match the profile ID and view in the query string
     */
    private final static Pattern QUERY_STRING_PROFILE_AND_VIEW_PATTERN = Pattern.compile(".*profile=([^&$]*)?.*");

    @Autowired
    @Setter // For testing
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
        if (intercepterHelper.requestHasValidCollectionId(request)) {

            String collectionId = intercepterHelper.getCollectionFromRequest(request);
            
            if(collectionId == null) {
                log.trace("Access restriction allowing access when no collection is set.");
                return true;
            }
            
            Collection collection = configRepository.getCollection(collectionId);
            if (collection == null) {
                // You asked for a collection which doesn't exist, so no access restriction applies.
                // (otherwise no one could ever get to the collection listing page)
                log.trace("Access restriction allowing access to nonexistent collection" + collectionId);
                return true;
            }
            
            String profileId = new ProfilePicker().existingProfileForCollection(collection, intercepterHelper.getProfileFromRequestOrDefaultProfile(request));
            
            ServiceConfigReadOnly serviceConfig;
            try {
                serviceConfig = configRepository.getServiceConfig(collectionId, profileId);
            } catch (ProfileNotFoundException e) {
                // profile picker always returns a profile that it thinks exists or one that is supposed to exist e.g. defailt_profile.
                throw new InvalidCollectionException(collectionId + " appears to exist but is invalid as it is missing the '" 
                                                        + profileId + "' profile which is expected to exist.");
            }
            
            if (serviceConfig.get(FrontEndKeys.ACCESS_RESTRICTION).isPresent()) {
                String accessRestriction = serviceConfig.get(FrontEndKeys.ACCESS_RESTRICTION).get();
                log.trace(Keys.ACCESS_RESTRICTION + " = '" + accessRestriction + "' for collection '" + collectionId + ":" + profileId + "'");
                if (DefaultValues.NO_RESTRICTION.equals(accessRestriction)) {
                    log.debug("Access restriction explicitely disabled. Granting access to " + collectionId + ":" + profileId);
                    return true;
                } else if (DefaultValues.NO_ACCESS.equals(accessRestriction)) {
                    log.debug("Access restriction expliciltely set to " + DefaultValues.NO_ACCESS + "for " + collectionId + ":" + profileId + ". Denying access");
                    denyAccess(request, response, serviceConfig, collectionId + ":" + profileId);
                    return false;
                } else {
                    String ip = getConnectingIp(request, serviceConfig);
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
                            log.warn("Access will be denied because: '" + Keys.ACCESS_RESTRICTION + 
                                "' in " + collectionId + ":" + profileId + " is misconfigured, IP ranges must be in CIDR format.");
                            denyAccess(request, response, serviceConfig, collectionId + ":" + profileId);
                            return false;
                        } else {
                            // It's a hostname
                            if (hostName.matches("^.*" + range + "$")) {
                                log.debug("'" + hostName + "' matches '" + range + "'. Granting access to '" + collectionId + ":" + profileId + "'");
                                return true;
                            }
                        }
                    }
                    log.debug("Neither IP '" + ip + "' or hostname '" + hostName + "' matched. Denying access to '" + collectionId + ":" + profileId + "'");
                    denyAccess(request, response, serviceConfig, collectionId + ":" + profileId);
                    return false;
                }
            } else {
                log.debug("No " + Keys.ACCESS_RESTRICTION + " setting for " + collectionId + ":" + profileId + "'");
            }
        }
        return true;
    }
    
    /**
     * Denies access by either returning a 403, or redirecting to an alternate collection.
     * @param request
     * @param response
     * @param collectionAndProfile
     * @param message Message to be displayed.
     * @throws IOException
     */
    private void denyAccess(HttpServletRequest request, 
                                HttpServletResponse response, 
                                ServiceConfigReadOnly serviceConfig, 
                                String collectionAndProfile) throws IOException {
        Optional<String> accessAlternate = serviceConfig.get(FrontEndKeys.ACCESS_ALTERNATE);
        if (accessAlternate.isPresent() && !accessAlternate.get().trim().isEmpty()) {
            StringBuffer out = new StringBuffer(request.getContextPath()).append(request.getPathInfo());
            
            Matcher m = QUERY_STRING_COLLECTION_PATTERN.matcher(request.getQueryString());
            if (m.find()) {
                out.append("?")
                    .append(request.getQueryString().substring(0, m.start(1)))
                    .append(serviceConfig.get(FrontEndKeys.ACCESS_ALTERNATE).get())
                    .append(request.getQueryString().substring(m.end(1), request.getQueryString().length()));
            } else {
                // No collection in the initial request, should not happen as we should have been
                // unable to check ACCESS_RESTRICTION at the first place
                throw new IllegalStateException(i18n.tr("parameter.missing", RequestParameters.COLLECTION));
            }
            
            log.debug("Applying access alternate setting for '" + collectionAndProfile + "'. Redirecting to '" + out.toString() + "'");
            response.sendRedirect(out.toString());
        } else {
            // No access_alternate. Simply deny access
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain");
            response.getWriter().write(i18n.tr("access.profile.denied", collectionAndProfile));
        }
    }
    
    public String getConnectingIp(HttpServletRequest request, ServiceConfigReadOnly serviceConfig) {
        String ip = request.getRemoteAddr();
        log.trace("Real request IP (ignoring X-Forwarded-For): " + ip);
        if (serviceConfig.get(FrontEndKeys.AccessRestriction.PREFER_X_FORWARDED_FOR)) {
            ip = NetUtils.getIpPreferingXForwardedFor(ip
                    , request.getHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR)
                    , serviceConfig.get(FrontEndKeys.AccessRestriction.IGNORED_IP_RANGES).get());
        }
        log.trace("Connecting IP is: " + ip);
        return ip;
    }

}
