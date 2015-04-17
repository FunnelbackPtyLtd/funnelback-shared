package com.funnelback.publicui.search.service.log;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.codec.digest.DigestUtils;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.net.NetUtils;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Utilities for queries / click logging
 *
 */
@Log4j2
public class LogUtils {

    /** HTTP Referer header */
    private static final String REFERER = "referer"; 
    
    /**
     * Get the request identifier depending of the type of identifier needed.
     * @param request The source HTTP request
     * @param idType Type of identifier needed
     * @param ignoredXForwardedForIPRanges The ip ranges in the X-Forwarded-For header to be ignored, in CIDR notation
     * @return The transformed address
     */
    public static String getRequestIdentifier(HttpServletRequest request, 
    		DefaultValues.RequestId idType,
    		String ignoredXForwardedForIPRanges) {
        if (request == null) {
            return Log.REQUEST_ID_NOTHING;
        }
        
        try {
            switch(idType) {
            case ip_hash:
                return DigestUtils.md5Hex(InetAddress.getByName(selectIp(request, ignoredXForwardedForIPRanges)).getHostAddress());
            case nothing:
                return Log.REQUEST_ID_NOTHING;
            case ip:
            default:
                return InetAddress.getByName(selectIp(request, ignoredXForwardedForIPRanges)).getHostAddress();
            }
        } catch (UnknownHostException uhe) {
            log.warn("Unable to get a user id from adress '"+request.getRemoteAddr()+"', for mode '" + idType + "'",
                uhe);
            return Log.REQUEST_ID_NOTHING;
        }
    }
    
    /**
     * @param s Search session to lookup the user id from
     * @return the user id from the search session, or null if the session is
     * null or doesn't have a user
     */
    public static String getUserId(SearchSession s) {
        if (s != null) {
            return getUserId(s.getSearchUser());
        }
        
        return null;
    }
    
    /**
     * @param user user to lookup the id from
     * @return the user id, or null if the user is null
     */
    public static String getUserId(SearchUser user) {
        if (user != null) {
            return user.getId();
        }
        
        return null;
    }

    /**
     * Helper method to get the HTTP referrer out of a request
     * 
     * @param request 
     * @return the URL for the HTTP referrer.
     */
    public static URL getReferrer(HttpServletRequest request) {
        URL referer = null;
        if (request.getHeader(REFERER) != null) {
            try {
                referer = new URL(request.getHeader(REFERER));
            } catch (MalformedURLException mue) {
                log.warn(
                        "Unable to parse referer '"
                                + request.getHeader(REFERER) + "'", mue);
            }
        }
        return referer;
    }
    
    /**
     * Helper method to cenerate a {@link CartClickLog} from the supplied args
     * 
     * @param targetUri
     * @param request
     * @param collection
     * @param logType
     * @param user
     * @return cartlog to log
     */
    public static CartClickLog createCartLog(URI targetUri, HttpServletRequest request, Collection collection, CartClickLog.Type logType, SearchUser user) {
        // Gather information for logging cart
        String requestId = LogUtils.getRequestIdentifier(request,
                DefaultValues.RequestId.valueOf(collection.getConfiguration()
                		.value(Keys.REQUEST_ID_TO_LOG, 
                				DefaultValues.REQUEST_ID_TO_LOG.toString())),
                				collection.getConfiguration()
                		.value(Keys.Logging.IGNORED_X_FORWARDED_FOR_RANGES,
                				DefaultValues.Logging.IGNORED_X_FORWARDED_FOR_RANGES));
        
        URL referer = LogUtils.getReferrer(request);
    	return new CartClickLog(new Date(), collection, collection.getProfiles()
        		.get(DefaultValues.DEFAULT_PROFILE), requestId, referer,
                targetUri, logType, LogUtils.getUserId(user));
    }
    
    /**
     * Select the first non-private IP from the X-Forwarded-For header,
     * or return the request remote address if there's no X-Forwarded-For
     * @param request HTTP request
     * @param ignoredXForwardedForIPRanges The ip ranges in the X-Forwarded-For header to be ignored, in CIDR notation
     * @return IP address
     */
    private static String selectIp(HttpServletRequest request, String ignoredXForwardedForIPRanges) {
        	return NetUtils.getIpPreferingXForwardedFor(request.getRemoteAddr()
    				, request.getHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR)
    				, ignoredXForwardedForIPRanges);
    }
    
}
