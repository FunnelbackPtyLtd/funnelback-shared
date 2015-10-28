package com.funnelback.publicui.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.funnelback.springmvc.api.config.security.SecurityConfigBase;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends SecurityConfigBase {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
            .and()
            .rememberMe()
            .rememberMeServices(tokenBasedForgotPasswordRememberMeServices());
    }

}
