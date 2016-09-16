package com.funnelback.publicui.search.web.interceptors;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Only allows access on the Admin port, or an additional developement
 * port. If access is attempted on a non-permitted port, users are redirected
 * to the admin port.
 */
@RequiredArgsConstructor
public class AdminPortOnlyRestrictionInterceptor implements
        HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response, Object handler, Exception exception)
        throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Autowired
    @Setter(AccessLevel.PROTECTED)
    private ConfigRepository configRepository;
    
    /**
     * Name of the Config setting containing the additional
     * port to permit
     */
    private final String additionalPortSettingName;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        Config globalCfg = configRepository.getGlobalConfiguration();
        
        // We only allow access on the admin port
        String adminUrlPort = globalCfg.value(Keys.Urls.ADMIN_PORT);
        String adminJettyPort = globalCfg.value(Keys.Jetty.ADMIN_PORT);
        
        // Also permit an additional development port (handy if you're running it in eclipse or from maven etc)
        String additionalAdminPort = globalCfg.value(additionalPortSettingName);
        
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
