package com.funnelback.publicui.search.web.filters;

import java.io.OutputStream;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.funnelback.publicui.search.web.filters.utils.InterceptableHttpServletResponseWrapper;

/**
 * Interface for groovy servlet filter hooks within Funnelback's Public UI,
 * allowing code to audit log or manipulate the request/response before it is
 * processed/returned to the user.
 * 
 * One instance of this class will be created per request processed.
 * 
 * @see com.funnelback.publicui.search.web.filters.GroovyFilter GroovyFilter -
 *      Where implementations are invoked
 */
public class GroovyServletFilterHook {

    /**
     * Allows the GroovyServletFilterHook implementation to perform actions
     * before the request reaches Funnelback.
     * 
     * Actions may include altering the request in some way, or wrapping the
     * response such that subsequent Funnelback processing can be controlled.
     * 
     * @param request
     *            Representation of the user's original request. Could be
     *            altered at this time before Funnelback sees it (though using a
     *            pre_process hook script would normally be preferred)
     * @param response
     *            The response object to which Funnelback's response will be
     *            written, unless it is changed/wrapped by this method's return
     *            value.
     * @return By default, the response provided. An alternate or wrapped
     *         response
     * @see com.funnelback.publicui.search.web.filters.utils.
     *      InterceptableHttpServletResponseWrapper
     *      InterceptableHttpServletResponseWrapper - May be helpful in wrapping
     *      the response.
     */
    public ServletResponse preFilterResponse(ServletRequest request, ServletResponse response) {
        return response;
    }

    /**
     * Allows the GroovyServletFilterHook implementation to perform actions
     * after Funnelback has processed the request.
     * 
     * @param request
     *            Representation of the user's original request. Useful as a
     *            reference, but it will be too late to meaningfully modify the
     *            request when this method runs.
     * @param response
     *            Representation of the Funnelback response to the user's
     *            request. Unless the response was wrapped above, it may be too
     *            late to change it, but details could be logged here. If the
     *            response were wrapped appropriately, sending of response
     *            content to the user may have been delayed, making changing of
     *            response headers or even content possible here.
     */
    public void postFilterResponse(ServletRequest request, ServletResponse response) {
    }

}
