package com.funnelback.publicui.search.service.log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j;

import org.apache.commons.codec.digest.DigestUtils;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.net.NetUtils;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Utilities for queries / click logging
 *
 */
@Log4j
public class LogUtils {
    
    /**
     * Get the request identifier depending of the type of identifier needed.
     * @param request The source HTTP request
     * @param idType Type of identifier needed
     * @return The transformed address
     */
    public static String getRequestIdentifier(HttpServletRequest request, DefaultValues.RequestId idType) {
        if (request == null) {
            return Log.REQUEST_ID_NOTHING;
        }
        
        try {
            switch(idType) {
            case ip_hash:
                return DigestUtils.md5Hex(InetAddress.getByName(request.getRemoteAddr()).getHostAddress());
            case nothing:
                return Log.REQUEST_ID_NOTHING;
            case ip:
            default:
                return InetAddress.getByName(selectIp(request)).getHostAddress();
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
     * Select the first non-private IP from the X-Forwarded-For header,
     * or return the request remote address if there's no X-Forwarded-For
     * @param request HTTP request
     * @return IP address
     */
    private static String selectIp(HttpServletRequest request) {
        if (request.getHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR) != null) {
            for (String ip: request.getHeader(SearchQuestion.RequestParameters.Header.X_FORWARDED_FOR).split(",")) {
                if (! NetUtils.isPrivateIp(ip.trim())) {
                    return ip.trim();
                }
            }
        }
        
        return request.getRemoteAddr();
    }
    
}
