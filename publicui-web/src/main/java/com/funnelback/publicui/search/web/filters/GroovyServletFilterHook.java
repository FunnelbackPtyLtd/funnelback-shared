package com.funnelback.publicui.search.web.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Interface for groovy filters within Funnelback's Public UI, allowing code to
 * audit log or manipulate the output before it is returned to the user.
 * 
 * @see com.funnelback.publicui.search.web.filters.GroovyFilter GroovyOutputFilter - Where implementatiosn are invoked
 */
public interface GroovyServletFilterHook {

    /**
     * This method is called once all Funnelback Public UI processing has taken
     * place, and
     * 
     * @param request
     *            Details of the request which the user initiated. At the time
     *            this filtering method is called it is too late to meaningfully
     *            modify any request properties, however it may be useful to
     *            read out request details.
     * @param bytes
     *            The byte array content of the Funnelback Public UI response.
     *            Unless the filter method returns a non-null result, these
     *            bytes will be returned to the caller.
     * @param response
     *            The response object under which they bytes above will be
     *            returned. It may be useful for filters to interrogate or
     *            modify response headers. Please note that anything written to
     *            the response object's writer/outputStream by the implementer
     *            will be ignored.
     * @return Either an updated array of bytes to return to the caller, or null
     *         (indicating no change to the content). Note that the response
     *         object's content length will automatically be updated to match
     *         the length of any returned byte array before the content is
     *         returned.
     */
    byte[] postFilterResponse(ServletRequest request, byte[] bytes, ServletResponse response);

}
