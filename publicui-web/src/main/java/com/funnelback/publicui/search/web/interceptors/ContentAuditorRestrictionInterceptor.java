package com.funnelback.publicui.search.web.interceptors;

import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Keys;
import com.funnelback.contentoptimiser.ContentOptimiserUserRestrictions;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Checks if early or late binding DLS is enabled, and then denys access if
 * either is enabled.
 */
public class ContentAuditorRestrictionInterceptor implements
        HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest arg0,
        HttpServletResponse arg1, Object arg2, Exception arg3)
        throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
            Object arg2, ModelAndView arg3) throws Exception {
    }

    @Autowired
    private ConfigRepository configRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        // We only allow access on the admin port
        String adminUrlPort = configRepository.getGlobalConfiguration().value(Keys.Urls.ADMIN_PORT);
        String adminJettyPort = configRepository.getGlobalConfiguration().value(Keys.Jetty.ADMIN_PORT);
        
        // Also permit an additional development port (handy if you're running it in eclipse or from maven etc)
        String additionalAdminPort = configRepository.getGlobalConfiguration().value(Keys.ModernUI.ContentAuditor.ADDITIONAL_PORT);
        
        String actualPort = Integer.toString(request.getLocalPort());
        if (actualPort.equals(adminJettyPort) || actualPort.equals(additionalAdminPort)) {
            return true;
        } else {
            // Redirect them to the right place - The admin version (so they get a login)
            URI originalUri = new URI(request.getRequestURL().toString());
            URI redirectUri = new URI("https", originalUri.getUserInfo(), originalUri.getHost(),
                Integer.parseInt(adminUrlPort), originalUri.getPath(), request.getQueryString(), null);
                        
            response.sendRedirect(redirectUri.toString());
            return false;
        }
    }

}
