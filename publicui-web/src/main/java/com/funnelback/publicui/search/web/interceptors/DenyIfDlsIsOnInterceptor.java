package com.funnelback.publicui.search.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.security.DLSEnabledChecker;

/**
 * Checks if early or late binding DLS is enabled, 
 * and then denies access if either is enabled.
 */
@Log4j2
public class DenyIfDlsIsOnInterceptor implements HandlerInterceptor {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    @Setter private DLSEnabledChecker dLSEnabledChecker;

    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response, Object o, Exception e)
        throws Exception { }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, ModelAndView mav) throws Exception {    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        if (request.getParameter(RequestParameters.COLLECTION) != null) {
            Collection collection = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
            if(collection == null) {
                return true;
            }
            
            if(dLSEnabledChecker.isDLSEnabled(collection)) {
                log.warn("Blocked access - DLS is enabled on " + collection.getConfiguration().getCollectionName());
                return false;
            }
            
            Config config = collection.getConfiguration();
            if(Type.meta.equals(config.getCollectionType())) {
                for(String component : collection.getMetaComponents()) {
                    collection = configRepository.getCollection(component);
                    if(collection != null && dLSEnabledChecker.isDLSEnabled(collection)) {
                        log.warn("Blocked access - DLS is enabled on " + collection.getConfiguration().getCollectionName());
                        return false;
                    }
                }
            }
            
        } 
        
        return true;
    }

}
