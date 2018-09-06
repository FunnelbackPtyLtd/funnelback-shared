package com.funnelback.publicui.search.web.filters;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.web.interceptors.helpers.IntercepterHelper;
import com.funnelback.publicui.utils.web.ProfilePicker;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.exec.OS;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.DelegatingFilterProxy;

import waffle.servlet.NegotiateSecurityFilter;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.filters.utils.FilterParameterHandling;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

/**
 * <p>Wrapper around the WAFFLE filter so that it can be disabled
 * easily in the publicui.properties file, instead of having to
 * edit <code>web.xml</code>.</p>
 * 
 * <p>I was forced to write a wrapper for {@link FilterConfig} to remove
 * the <em>targetFilterLifecycle</em> <code>&lt;init-param&gt;</code>. This param is a
 * Spring-specific parameter (As we use the {@link DelegatingFilterProxy} facility) and
 * it's passed along to WAFFLE, which doesn't recognize it and throws an exception.</p>
 */
@Log4j2
public class ConfigurableSecurityFilter extends NegotiateSecurityFilter {

    @Autowired
    private ConfigRepository configRepository;

    private IntercepterHelper intercepterHelper = new IntercepterHelper();

    private boolean active = false;
    
    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(new FilterConfigWrapper(config, new String[] {"targetFilterLifecycle"}));
    
        if (OS.isFamilyWindows()) {
            active = true;
            log.debug("Windows authentication filter is loaded. Use ui.modern.authentication=true to activate it on collections");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {

        if (active) {
            String collectionId = new FilterParameterHandling().getCollectionId((HttpServletRequest) request);

            if (collectionId != null) {
                Collection collection = configRepository.getCollection(collectionId);
                String profileId = new ProfilePicker().existingProfileForCollection(collection, intercepterHelper.getProfileFromRequestOrDefaultProfile((HttpServletRequest) request));
                ServiceConfigReadOnly serviceConfig;
                try {
                    serviceConfig = configRepository.getServiceConfig(collectionId, profileId);
                    if (collection != null && serviceConfig.get(FrontEndKeys.ModernUi.AUTHENTICATION)) {
                        log.debug("Using Windows authentication filter for collection '"+collectionId+"'");
                        super.doFilter(request, response, chain);
                        return;
                    }
                } catch (ProfileNotFoundException e) {
                    log.error("Couldn't find profile '" + profileId + "' in " + collectionId, e);
                }
            }
        }

        chain.doFilter(request, response);
    }
        
    /**
     * Wrapper around {@link FilterConfig} to allow customization
     * of init parameters.
     */
    public class FilterConfigWrapper implements FilterConfig {

        private String[] hiddenParamNames;
        private FilterConfig filterConfig;
        private Set<String> modifiedParameterNames = new HashSet<String>();
        
        public FilterConfigWrapper(FilterConfig filterConfig, String[] hiddenParamNames) {
            this.filterConfig = filterConfig;
            this.hiddenParamNames = hiddenParamNames;

            @SuppressWarnings("rawtypes")
            Enumeration parameterNames = filterConfig.getInitParameterNames();
            while(parameterNames.hasMoreElements()) {
                String name = (String) parameterNames.nextElement();
                if (! ArrayUtils.contains(hiddenParamNames, name)) {
                    modifiedParameterNames.add(name);
                }
            }
                
        }
        
        @Override
        public String getFilterName() {
            return filterConfig.getFilterName();
        }

        @Override
        public String getInitParameter(String name) {
            if (ArrayUtils.contains(hiddenParamNames, name)) {
                return null;
            } else {
                return filterConfig.getInitParameter(name);
            }
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Enumeration getInitParameterNames() {
            return Collections.enumeration(modifiedParameterNames);
        }

        @Override
        public ServletContext getServletContext() {
            return filterConfig.getServletContext();
        }
        
    }
    
}
