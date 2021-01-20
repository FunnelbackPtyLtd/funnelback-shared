package com.funnelback.plugin.servlet.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * Base class for servlet filter hooks within Funnelback's Public UI,
 * allowing code to audit log or manipulate the request/response before it is
 * processed/returned to the user.
 *
 * One instance of this class will be created per request processed.
 *
 */
public interface SearchServletFilterHook {

    /**
     * Allows the ServletFilterHook implementation to perform actions on
     * the request before it is populated by Funnelback.
     *
     * @param context under which the request is running. Contains Funnelback specific
     *                details not available in the Servlet APIs.
     * @param request
     *            Representation of the user's original request. May be wrapped
     *            and returned to affect request processing.
     * @return By default, the request provided. Potentially an alternate or
     *         wrapped request.
     */
    default ServletRequest preFilterRequest(
            SearchServletFilterHookContext context, ServletRequest request) {
        return request;
    }

    /**
     * Allows the ServletFilterHook implementation to perform actions on
     * the response before it is populated by Funnelback, or prevent further
     * processing (by returning null).
     *
     * @param context under which the request is running. Contains Funnelback specific
     *                details not available in the Servlet APIs.
     * @param request
     *            Representation of the user's request (useful in limiting
     *            actions to only certain types of requests).
     * @param response
     *            The response object to which Funnelback's response will be
     *            written, unless it is changed/wrapped by this method's return
     *            value.
     * @return By default, the response provided. Potentially an alternate or
     *         wrapped response. May return null, in which case the hook is assumed
     *         to have already responded (for example, it already returned an
     *         error to the user for some reason), and Funnelback will do no
     *         further processing on the request (and postFilterResponse will
     *         not be run).
     */
    default ServletResponse preFilterResponse(
            SearchServletFilterHookContext context, ServletRequest request, ServletResponse response) {
        return response;
    }

    /**
     * Allows the ServletFilterHook implementation to perform actions
     * after Funnelback has processed the request.
     *
     * @param context under which the request is running. Contains Funnelback specific
     *                details not available in the Servlet APIs.
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
    default void postFilterResponse(
            SearchServletFilterHookContext context, ServletRequest request, ServletResponse response) { }

}
