package com.funnelback.publicui.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.api.config.security.ProtectAllHttpBasicAndTokenSecurityConfig;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends ProtectAllHttpBasicAndTokenSecurityConfig {
    
    @Autowired
    ExecutionContextHolder executionContextHolder;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        switch (executionContextHolder.getExecutionContext()) {
        case Admin:
            super.configureHttpbasicAndToken(http);
            break;
        case Novell:
            break;
        case Public:
            http
                .authorizeRequests()
                .anyRequest()
                .permitAll();
            break;
        case Unknown:
            break;
        default:
            break;
        }
        //Disable csrf, as we don't care to protect anything here with it.
        http.csrf().disable();
        
        // This is usually done in the parent however as we don't actually
        // call the parent to protect this end point when in non Admin mode
        // we need to ensure this is disabled here.
        http.headers().httpStrictTransportSecurity().disable();
    }
}
