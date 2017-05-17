package com.funnelback.publicui.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
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
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.api.config.security.FunnelbackAdminAuthenticationProvider;
import com.funnelback.springmvc.api.config.security.ProtectAllHttpBasicAndTokenSecurityConfig;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends ProtectAllHttpBasicAndTokenSecurityConfig {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    ExecutionContextHolder executionContextHolder;

    /* SAML Related Beans - see SamlConfig */
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
     * Defines the web based security configuration.
     * 
     * @param   http It allows configuring web based security for specific http requests.
     * @throws  Exception 
     */
    @Override  
    protected void configure(HttpSecurity http) throws Exception {
        boolean requireX509Authentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.REQUIRE_X509, false);
        boolean enableSamlAuthentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.ENABLE_SAML, false);
        
        if (requireX509Authentication) {
            http
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .x509().userDetailsService(new X509UserDetailsService());
        } else if (enableSamlAuthentication) {
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
        super.configureGlobal(auth, funnelbackAdminAuthenticationProvider);

        boolean enableSamlAuthentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.ENABLE_SAML, false);

        if (enableSamlAuthentication) {
            auth
                .authenticationProvider(samlAuthenticationProvider);
        }
    } 
    
    // Central storage of cryptographic keys
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public KeyManager keyManager() {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader
                .getResource("classpath:/saml/samlKeystore.jks");
        String storePass = "nalle123";
        Map<String, String> passwords = new HashMap<String, String>();
        passwords.put("apollo", "nalle123");
        String defaultKey = "apollo";
        
        return new JKSKeyManager(storeFile, storePass, passwords, defaultKey);
    }
 
    // Setup TLS Socket Factory
    // Allow all certificates to avoid SSL failures on metadata
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public TLSProtocolConfigurer tlsProtocolConfigurer() {
        TLSProtocolConfigurer configurer = new TLSProtocolConfigurer();
        configurer.setSslHostnameVerification("allowAll");
        return configurer;
    }
    
    // Setup advanced info about metadata
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(false); 
        extendedMetadata.setSslHostnameVerification("allowAll");
        extendedMetadata.setSignMetadata(false);
        return extendedMetadata;
    }
    
    @Bean
    @Qualifier("idp-mujina")
    @Conditional(IsSamlEnabledCondition.class)
    public ExtendedMetadataDelegate mujinaExtendedMetadataProvider()
            throws MetadataProviderException {
        String mujinaMetadataURL = System.getProperty("mujinaBaseUrl", "http://localhost:8080") + "/metadata";
        Timer backgroundTaskTimer = new Timer(true);
        HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
                backgroundTaskTimer, new HttpClient(new MultiThreadedHttpConnectionManager()), mujinaMetadataURL);

        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        try {
            parserPool.initialize();
        } catch (XMLParserException e) {
            throw new MetadataProviderException("Exception initialising XML parser", e);
        }
        httpMetadataProvider.setParserPool(parserPool);
        
        ExtendedMetadataDelegate extendedMetadataDelegate =
                new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
        extendedMetadataDelegate.setMetadataTrustCheck(true);
        extendedMetadataDelegate.setMetadataRequireSignature(false);

        return extendedMetadataDelegate;
    }


    // IDP Metadata configuration - paths to metadata of IDPs in circle of trust
    // is here
    // Do no forget to call iniitalize method on providers
    @Bean
    @Qualifier("metadata")
    @Conditional(IsSamlEnabledCondition.class)
    public CachingMetadataManager metadata() throws MetadataProviderException {
        List<MetadataProvider> providers = new ArrayList<MetadataProvider>();

        ExtendedMetadataDelegate mujinaEmd = mujinaExtendedMetadataProvider();
        mujinaEmd.initialize();
        providers.add(mujinaEmd);
   
        CachingMetadataManager manager = new CachingMetadataManager(providers);
      
//        // Todo: add to com.funnelback.common.config.Keys
//        String defaultIDP = configRepository
//                    .getGlobalConfiguration().value("saml.default_idp");
//        if (defaultIDP != null) {
//            manager.setDefaultIDP(defaultIDP); 
//        } else { 
//           manager.setDefaultIDP("");
//        }
 
        return manager; 
    }
 
    // Filter automatically generates default SP metadata
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public MetadataGenerator metadataGenerator() {
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        // Todo: add to com.funnelback.common.config.Keys
//        String entityID = configRepository
//                    .getGlobalConfiguration().value("saml.entity_id");
//        if (entityID != null) {
//             metadataGenerator.setEntityId(entityID);
//        } else {
//             metadataGenerator.setEntityId("com:funnelback:publicui:sp"); 
//        }
        String entityId = "http://mock-idp";
        metadataGenerator.setExtendedMetadata(extendedMetadata());
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setKeyManager(keyManager()); 
        return metadataGenerator;
    }
    
    // Handler deciding where to redirect user after successful login
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public static SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successRedirectHandler.setDefaultTargetUrl("/search.html");
        return successRedirectHandler;
    }

    // Handler deciding where to redirect user after failed login
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public static SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/error");
        return failureHandler;
    }

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
