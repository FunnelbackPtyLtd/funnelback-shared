package com.funnelback.publicui.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.api.config.security.FunnelbackAdminAuthenticationProvider;
import com.funnelback.springmvc.api.config.security.ProtectAllHttpBasicAndTokenSecurityConfig;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@Log4j2
public class SecurityConfig extends ProtectAllHttpBasicAndTokenSecurityConfig {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    ExecutionContextHolder executionContextHolder;

    /**
     * Defines the web based security configuration.
     * 
     * @param   http It allows configuring web based security for specific http requests.
     * @throws  Exception 
     */
    @Override  
    protected void configure(HttpSecurity http) throws Exception {
        boolean requireX509Authentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.REQUIRE_X509, false);
        boolean enableSamlAuthentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.SAML.ENABLED, false);
        
        if (requireX509Authentication) {
            log.info("Configuring publicui security with x.509 client certificate requirement");
            http
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .x509().userDetailsService(new X509UserDetailsService());
        } else if (enableSamlAuthentication) {
            log.info("Configuring publicui security with SAML");
            // If SAML is on, we always authenticate search results
            // regardless of the ExecutionContext.
            //
            // Note: we still allow HttpBasic and Token auth alongside SAML
            // but HttpBasic must be proactive (we'll never return 401).
            http.rememberMe()
                .rememberMeServices(tokenBasedRememberMeServices());

            // This causes us to have httpBasic enabled, but if it fails
            // (because there's no header, samlEntryPoint is used to
            // initiate authentication (which redirects the user
            // off to the IdP's login process rather than returning 401).
            http
                .httpBasic()
                    .authenticationEntryPoint(samlEntryPoint);

            http
                .addFilterBefore(metadataGeneratorFilter, ChannelProcessingFilter.class)
                .addFilterAfter(samlFilter, BasicAuthenticationFilter.class);

            http
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers("/saml/**").permitAll()
                .anyRequest().authenticated();
            http.logout().logoutSuccessUrl("/");
        } else {
            log.info("Configuring default publicui security (admin basic/token, search public)");
            if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
                super.configureHttpbasicAndToken(http);
            } else if (ExecutionContext.Public.equals(executionContextHolder.getExecutionContext())) {
                http.authorizeRequests().anyRequest().permitAll();
            } else {
                // Do nothing - These get no security by default.
            }
        }
        
        // Disable csrf, as we don't care to protect anything here with it.
        http.csrf().disable();

        // This is usually done in the parent however as we don't actually
        // call the parent to protect this end point when in non Admin mode
        // we need to ensure this is disabled here.
        http.headers().httpStrictTransportSecurity().disable();
    }

    @Autowired
    FunnelbackAdminAuthenticationProvider funnelbackAdminAuthenticationProvider;

    /**
     * Sets a custom authentication provider.
     * 
     * @param   auth SecurityBuilder used to create an AuthenticationManager.
     * @throws  Exception 
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configureGlobal();

        boolean enableSamlAuthentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.SAML.ENABLED, false);

        if (enableSamlAuthentication) {
            auth.authenticationProvider(samlAuthenticationProvider);
        }
    } 
    
    /* 
     * SAML Related Beans - see SamlConfig and SamlBoilerplateConfig
     * 
     * Autowired is not 'required' because SAML may not be enabled
     */
    @Autowired(required=false)
    SAMLEntryPoint samlEntryPoint;
    
    @Autowired(required=false)
    MetadataGeneratorFilter metadataGeneratorFilter;
    
    @Autowired(required=false)
    @Qualifier("samlFilter")
    FilterChainProxy samlFilter;
    
    @Autowired(required=false)
    SAMLAuthenticationProvider samlAuthenticationProvider;
    
    /**
     * Returns the authentication manager currently used by Spring.
     * It represents a bean definition with the aim allow wiring from
     * other classes performing the Inversion of Control (IoC).
     * 
     * @throws  Exception 
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
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
