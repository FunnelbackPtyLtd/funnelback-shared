package com.funnelback.publicui.search.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.service.ConfigRepository;

import lombok.extern.log4j.Log4j2;
/**
 * Filter which Pseudonomises client IP addresses by stripping the last octet of the address - e.g.
 * 123.123.123.123 becomes 123.123.123.0. This pseudonomysed address is intended to be not personally identifiable
 * while still producing the same approximate location information when processed with MaxMind's database
 */
@Log4j2
public class IpPseudonomysationFilter implements Filter {

    
    @Autowired
    private ConfigRepository configRepository;

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        
        configRepository.getGlobalConfiguration().valueAsBoolean("pseudonomise_client_ips", true);
        chain.doFilter(new PseudonymousServletRequest((HttpServletRequest)request), response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException { }

    public static class PseudonymousServletRequest extends HttpServletRequestWrapper {

        public PseudonymousServletRequest(HttpServletRequest request) {
            super(request);
        }
        
        public String getRemoteAddr() {
            return pseudonomyseRemoteAddress(getRequest().getRemoteAddr());
        }
        
        public static String pseudonomyseRemoteAddress(String originalRemoteAddress) {
            return originalRemoteAddress.replaceAll("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)", "$1.$2.$3.0");
        }
    }
    
}
