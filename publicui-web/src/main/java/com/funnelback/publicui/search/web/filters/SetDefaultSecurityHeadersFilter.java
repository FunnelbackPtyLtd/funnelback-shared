package com.funnelback.publicui.search.web.filters;

import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.keys.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.filters.utils.FilterParameterHandling;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.OnCommittedResponseWrapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * We don't let spring set security headers by default in publicui (see
 * SecurityConfig) because we can't then override them on a request-by-request
 * basis reliably (see RNDSUPPORT-3048) but we do (mostly) still want to have
 * these headers, so we set them as default early on here, allowing them to be
 * swapped out later as needed.
 */
@Log4j2
public class SetDefaultSecurityHeadersFilter implements Filter {

    public static Map<String, String> DEFAULT_SECURITY_HEADERS = Map.of(
        "Cache-Control","no-cache, no-store, max-age=0, must-revalidate",
        "Pragma","no-cache",
        "Expires","0",
        "X-Content-Type-Options","nosniff",
        // We don't want the Strict-Transport-Security one spring gives because we don't ensure search is available on https
        "X-Frame-Options","DENY",
        "X-XSS-Protection","1; mode=block"
        );

    @Override public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        DEFAULT_SECURITY_HEADERS.forEach((name, value) -> {
            httpServletResponse.setHeader(name, value);
        });

        chain.doFilter(request, httpServletResponse);
    }
}
