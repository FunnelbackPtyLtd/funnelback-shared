package com.funnelback.publicui.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.api.config.security.ProtectAllHttpBasicAndTokenSecurityConfig;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends ProtectAllHttpBasicAndTokenSecurityConfig {

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.
            inMemoryAuthentication()
                .withUser("ignoredusername").password("password").roles("USER");
        // As I understand it, this is required to provide a UserDetailsService, but
        // we don't actually care about the roles here (because we just care about
        // whether users were permitted at all or not.
    }

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
                .x509();
            // We don't map any special roles etc to certificate properties, but if a later groovy script or
            // something wants access to the certificate, I believe it can be accessed with something like
            // the following during a request.
            // 
            // ((X509Certificate)
            // SecurityContextHolder.getContext().getAuthentication().getCredentials()).getIssuerDN().getName()
            //
            // See http://stackoverflow.com/questions/37720622
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
}
