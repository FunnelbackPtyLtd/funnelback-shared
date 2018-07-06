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

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.frontend.modernui.ModernUI;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.filters.utils.FilterParameterHandling;
import com.funnelback.publicui.utils.web.ProfilePicker;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
/**
 * Filter which Pseudonomises client IP addresses by stripping the last octet of the address - e.g.
 * 123.123.123.123 becomes 123.123.123.0. This pseudonomysed address is intended to be not personally identifiable
 * while still producing the same approximate location information when processed with MaxMind's database
 */
@Log4j2
public class IpPseudonomysationFilter implements Filter {

    /**
     * This attribute name can be used to access the unpseudonomysed client IP address.
     * 
     * We should take care not to log this address, however it is important to have
     * for things like IP address access restrictions.
     */
    public static final String UNPSEUDONOMYSED_IP_ADDRESS_ATTRIBUTE_NAME = "com.funnelback.unpseudonomysed-ip-address";
    
    @Autowired
    @Setter
    private ConfigRepository configRepository;

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        
        FilterParameterHandling fph = new FilterParameterHandling();
        String collectionId = fph.getCollectionId((HttpServletRequest) request);
        String profileAndViewId = fph.getProfileAndViewId((HttpServletRequest) request);

        Boolean shouldPseudonomise;
        try {
            shouldPseudonomise = configRepository
                .getServiceConfig(collectionId, profileAndViewId)
                .get(new ModernUI().PSEUDONOMISE_CLIENT_IPS);
        } catch (ProfileNotFoundException e) {
            shouldPseudonomise = true;
        }
        
        chain.doFilter(new PseudonymousServletRequest((HttpServletRequest)request, shouldPseudonomise), response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException { }

    public static String pseudonomyseRemoteAddress(String originalRemoteAddress) {
        return originalRemoteAddress.replaceAll("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)", "$1.$2.$3.0");
    }

    public static class PseudonymousServletRequest extends HttpServletRequestWrapper {

        private Boolean shouldPseudonomise;

        public PseudonymousServletRequest(HttpServletRequest request, Boolean shouldPseudonomise) {
            super(request);
            this.shouldPseudonomise = shouldPseudonomise;
        }
        
        public String getRemoteAddr() {
            if (shouldPseudonomise) {
                return pseudonomyseRemoteAddress(getRequest().getRemoteAddr());
            } else {
                return getRequest().getRemoteAddr();
            }
        }
        
        // Provide an obscure-ish way to get the original IP address if
        // it's needed (e.g. for IP address restrictions).
        @Override
        public Object getAttribute(String key) {
            if (UNPSEUDONOMYSED_IP_ADDRESS_ATTRIBUTE_NAME.equals(key)) {
                return getRequest().getRemoteAddr();
            }
            return super.getAttribute(key);
        }
    }
    
}
