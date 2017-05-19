package com.funnelback.publicui.security;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.eclipse.jetty.util.security.Password;
import org.opensaml.saml2.metadata.provider.AbstractMetadataProvider;
import org.opensaml.saml2.metadata.provider.BaseMetadataProvider;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Service;

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
        boolean enableSamlAuthentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.SAML.ENABLED, false);
        
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

        boolean enableSamlAuthentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.SAML.ENABLED, false);

        if (enableSamlAuthentication) {
            auth.authenticationProvider(samlAuthenticationProvider);
        }
    } 
    
    // SAML Authentication Provider responsible for validating of received SAML
    // messages
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(new SAMLUserDetailsServiceImpl());
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }


    // See KeyManager doc in section 8.1 of
    // http://docs.spring.io/spring-security-saml/docs/current/reference/html/security.html
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public KeyManager keyManager() {
        String samlKeystorePath = configRepository.getGlobalConfiguration().value(Keys.Auth.PublicUI.SAML.KEYSTORE_PATH);
        String samlKeystorePassword = configRepository.getGlobalConfiguration().value(Keys.Auth.PublicUI.SAML.KEYSTORE_PASSWORD);
        String samlKeyAlias = configRepository.getGlobalConfiguration().value(Keys.Auth.PublicUI.SAML.KEY_ALIAS);
        String samlKeyPassword = configRepository.getGlobalConfiguration().value(Keys.Auth.PublicUI.SAML.KEY_PASSWORD);
        
        if (samlKeystorePassword.startsWith(Password.__OBFUSCATE)) {
            samlKeystorePassword = Password.deobfuscate(samlKeystorePassword);
        }
        if (samlKeyPassword.startsWith(Password.__OBFUSCATE)) {
            samlKeyPassword = Password.deobfuscate(samlKeyPassword);
        }
        
        FileSystemResourceLoader loader = new FileSystemResourceLoader();
        Resource samlKeystoreResource = loader.getResource("file://" + samlKeystorePath);

        Map<String, String> passwords = new HashMap<String, String>();
        passwords.put(samlKeyAlias, samlKeyPassword);
        // It's unclear to me why we'd ever want to provide
        // keys for other passwords here - Hopefully I'll
        // find out before we ship if there's a good one.
        
        return new JKSKeyManager(samlKeystoreResource, samlKeystorePassword, passwords, samlKeyAlias);
    }
 
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public TLSProtocolConfigurer tlsProtocolConfigurer() {
        return new TLSProtocolConfigurer();
    }
    
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public ExtendedMetadata extendedMetadata() {
        return new ExtendedMetadata();
    }
    
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public ExtendedMetadataDelegate extendedMetadataProvider()
            throws MetadataProviderException {
        String metadataURL = configRepository.getGlobalConfiguration().value(Keys.Auth.PublicUI.SAML.IDENTITY_PROVIDER_METADATA_URL, "");
        
        AbstractMetadataProvider metadataProvider;
        if (metadataURL.startsWith("file:")) {
            // Local filesystem
            try {
                File metadataFile = Paths.get(new URI(metadataURL)).toFile();
                metadataProvider = new FilesystemMetadataProvider(metadataFile);
            } catch (URISyntaxException e) {
                throw new MetadataProviderException("Invalid URI for metadata file", e);
            }
        } else {
            // We assume it's an HTTP[s] url
            metadataProvider = new HTTPMetadataProvider(
                new Timer(true), new HttpClient(new MultiThreadedHttpConnectionManager()), metadataURL);
        }


        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        try {
            parserPool.initialize();
        } catch (XMLParserException e) {
            throw new MetadataProviderException("Exception initialising SAML XML metadata parser", e);
        }
        metadataProvider.setParserPool(parserPool);
        
        ExtendedMetadataDelegate extendedMetadataDelegate =
                new ExtendedMetadataDelegate(metadataProvider, extendedMetadata());
        extendedMetadataDelegate.setMetadataTrustCheck(true);
        extendedMetadataDelegate.setMetadataRequireSignature(false);

        return extendedMetadataDelegate;
    }


    // IDP Metadata configuration - paths to metadata of IDPs in circle of trust
    // is here
    @Bean
    @Qualifier("metadata")
    @Conditional(IsSamlEnabledCondition.class)
    public CachingMetadataManager metadata() throws MetadataProviderException {
        List<MetadataProvider> providers = new ArrayList<MetadataProvider>();

        ExtendedMetadataDelegate mujinaEmd = extendedMetadataProvider();
        mujinaEmd.initialize();
        providers.add(mujinaEmd);
   
        CachingMetadataManager manager = new CachingMetadataManager(providers);
 
        return manager; 
    }
 
    // Filter automatically generates default SP metadata
    @Bean
    @Conditional(IsSamlEnabledCondition.class)
    public MetadataGenerator metadataGenerator() {
        MetadataGenerator metadataGenerator = new MetadataGenerator();

        String entityId = configRepository.getGlobalConfiguration().value(Keys.Auth.PublicUI.SAML.ENTITY_ID, "");;
        metadataGenerator.setEntityId(entityId);
        metadataGenerator.setExtendedMetadata(extendedMetadata());
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
        //failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/search.html");
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
    
    public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {
        
        public Object loadUserBySAML(SAMLCredential credential)
                throws UsernameNotFoundException {
            
            // The method is supposed to identify local account of user referenced by
            // data in the SAML assertion and return UserDetails object describing the user.
            
            String userID = credential.getNameID().getValue();
            
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
            authorities.add(authority);

            // In a real scenario, this implementation has to locate user in a arbitrary
            // dataStore based on information present in the SAMLCredential and
            // returns such a date in a form of application specific UserDetails object.
            return new User(userID, "<abc123>", true, true, true, true, authorities);
        }
        
    }

}
