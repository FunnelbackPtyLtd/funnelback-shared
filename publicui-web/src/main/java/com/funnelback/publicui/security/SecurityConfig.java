package com.funnelback.publicui.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.api.config.security.ProtectAllHttpBasicAndTokenSecurityConfig;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends ProtectAllHttpBasicAndTokenSecurityConfig {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    ExecutionContextHolder executionContextHolder;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        boolean requireX509Authentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.REQUIRE_X509, false);

        if (requireX509Authentication) {
            http
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .x509().userDetailsService(new X509UserDetailsService());
        } else {
            switch (executionContextHolder.getExecutionContext()) {
            case Admin:
                super.configureHttpbasicAndToken(http);
                break;
            case Novell:
                break;
            case Public:
                http.authorizeRequests().anyRequest().permitAll();
                break;
            case Unknown:
                break;
            default:
                break;
            }
        }
        
        // Disable csrf, as we don't care to protect anything here with it.
        http.csrf().disable();

        // This is usually done in the parent however as we don't actually
        // call the parent to protect this end point when in non Admin mode
        // we need to ensure this is disabled here.
        http.headers().httpStrictTransportSecurity().disable();
    }

    public class X509UserDetailsService implements UserDetailsService {
        // This very simple user details service is intended to cause spring to
        // store the certificate, so that if it's needed later it can be fetched
        // with something like...
        //
        // ((X509Certificate)
        // SecurityContextHolder.getContext().getAuthentication().getCredentials()).getIssuerDN().getName()
        //
        // That gets the username from the certificate, but other values could
        // also be accessed
        //
        // See http://stackoverflow.com/questions/37720622
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return new User(username, "N/A", Arrays.asList(() -> "ROLE_SSL"));
        }
    }

}
