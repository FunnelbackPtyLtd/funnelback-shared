package com.funnelback.publicui.search.web.interceptors;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AccessLevel;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.config.configtypes.server.ServerConfigReadOnly;
import static com.funnelback.config.keys.Keys.ServerKeys;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Only allows access on the Admin port, or an additional developement
 * port. If access is attempted on a non-permitted port, users are redirected
 * to the admin port.
 */
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
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        ServerConfigReadOnly serverConfig = configRepository.getServerConfig();
        
        // We only allow access on the admin port
        int adminUrlPort = serverConfig.get(ServerKeys.Urls.ADMIN_PORT);
        int adminJettyPort = serverConfig.get(ServerKeys.Jetty.JETTY_ADMIN_PORT);
        
        // Also permit an additional development port (handy if you're running it in eclipse or from maven etc)
        int additionalAdminPort = serverConfig.get(ServerKeys.Urls.DEVELOPMENT_PORT); 
        
        int actualPort = request.getLocalPort();
        if (actualPort == adminJettyPort || actualPort == additionalAdminPort) {
            return true;
        } else {
            // Redirect them to the right place - The admin version (so they get a login)
            URI originalUri = new URI(request.getRequestURL().toString());
            URI redirectUri = new URI("https", originalUri.getUserInfo(), originalUri.getHost(),
                adminUrlPort, originalUri.getPath(), request.getQueryString(), null);
                        
            response.sendRedirect(redirectUri.toString());
            return false;
        }
    }

}
