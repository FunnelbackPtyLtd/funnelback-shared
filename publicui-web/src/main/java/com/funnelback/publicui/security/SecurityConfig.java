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
    }
}
