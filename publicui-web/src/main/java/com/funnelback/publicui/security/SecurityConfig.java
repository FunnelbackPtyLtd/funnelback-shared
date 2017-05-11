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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPArtifactBinding;
import org.springframework.security.saml.processor.HTTPPAOS11Binding;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.HTTPSOAP11Binding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.saml.trust.httpclient.TLSProtocolSocketFactory;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.ArtifactResolutionProfile;
import org.springframework.security.saml.websso.ArtifactResolutionProfileImpl;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileECPImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends ProtectAllHttpBasicAndTokenSecurityConfig {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    ExecutionContextHolder executionContextHolder;
    
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        switch (executionContextHolder.getExecutionContext()) {
//        case Admin:
//            super.configureHttpbasicAndToken(http);
//            break;
//        case Novell:
//            break;
//        case Public:
//            http
//                .authorizeRequests()
//                .anyRequest()
//                .permitAll();
//            break;
//        case Unknown:
//            break;
//        default:
//            break;
//        }
//        //Disable csrf, as we don't care to protect anything here with it.
//        http.csrf().disable();
//    }
    
    

    @Autowired
    private SAMLUserDetailsServiceImpl samlUserDetailsServiceImpl;
     
    // Initialization of the velocity engine
    @Bean
    public VelocityEngine velocityEngine() {
        return VelocityFactory.getEngine();
    }
 
    // XML parser pool needed for OpenSAML parsing
    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }
 
    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }
 
    // Bindings, encoders and decoders used for creating and parsing messages
    @Bean
    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
        return new MultiThreadedHttpConnectionManager();
    }
 
    @Bean
    public HttpClient httpClient() {
        return new HttpClient(multiThreadedHttpConnectionManager());
    }
 
    // SAML Authentication Provider responsible for validating of received SAML
    // messages
    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(samlUserDetailsServiceImpl);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }
 
    // Provider of default SAML Context
    @Bean
    public SAMLContextProviderImpl contextProvider() {
        return new SAMLContextProviderImpl();
    }
 
    // Initialization of OpenSAML library
    @Bean
    public static SAMLBootstrap sAMLBootstrap() {
        return new SAMLBootstrap();
    }
 
 
    // Central storage of cryptographic keys
    @Bean
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
    public TLSProtocolConfigurer tlsProtocolConfigurer() {
        TLSProtocolConfigurer configurer = new TLSProtocolConfigurer();
        configurer.setSslHostnameVerification("allowAll");
        return configurer;
    }
    
    @Bean
    public ProtocolSocketFactory socketFactory() {
        return new TLSProtocolSocketFactory(keyManager(), null, "default");
    }

    @Bean
    public Protocol socketFactoryProtocol() {
        return new Protocol("https", socketFactory(), 443);
    }

    @Bean
    public MethodInvokingFactoryBean socketFactoryInitialization() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(Protocol.class);
        methodInvokingFactoryBean.setTargetMethod("registerProtocol");
        Object[] args = {"https", socketFactoryProtocol()};
        methodInvokingFactoryBean.setArguments(args);
        return methodInvokingFactoryBean;
    }
    
    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        return webSSOProfileOptions;
    }
 
    // Entry point to initialize authentication, default values taken from
    // properties file
    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
        return samlEntryPoint;
    }
    
    // Setup advanced info about metadata
    @Bean
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(false); 
        extendedMetadata.setSslHostnameVerification("allowAll");
        extendedMetadata.setSignMetadata(false);
        return extendedMetadata;
    }
    
    // IDP Discovery Service
    @Bean
    public SAMLDiscovery samlIDPDiscovery() {
        SAMLDiscovery idpDiscovery = new SAMLDiscovery();
        idpDiscovery.setIdpSelectionPath("/saml/idpSelection");
        return idpDiscovery;
    }
    
//    @Bean
//    @Qualifier("idp-ssocircle")
//    public ExtendedMetadataDelegate ssoCircleExtendedMetadataProvider()
//            throws MetadataProviderException {
//        String idpSSOCircleMetadataURL = "http://idp.ssocircle.com/idp-meta.xml";
//        Timer backgroundTaskTimer = new Timer(true);
//        HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
//                backgroundTaskTimer, httpClient(), idpSSOCircleMetadataURL);
//        httpMetadataProvider.setParserPool(parserPool());
//        ExtendedMetadataDelegate extendedMetadataDelegate = 
//               new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
//        extendedMetadataDelegate.setMetadataTrustCheck(true);
//        extendedMetadataDelegate.setMetadataRequireSignature(false);
//        return extendedMetadataDelegate;
//    }

//    @Bean
//    @Qualifier("idp-openidp")
//    public ExtendedMetadataDelegate openIdpExtendedMetadataProvider()
//            throws MetadataProviderException {
//        String idpSSOCircleMetadataURL = "https://openidp.feide.no/simplesaml/saml2/idp/metadata.php";
//        Timer backgroundTaskTimer = new Timer(true);
//        HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
//                backgroundTaskTimer, httpClient(), idpSSOCircleMetadataURL);
//        httpMetadataProvider.setParserPool(parserPool());
//        ExtendedMetadataDelegate extendedMetadataDelegate = 
//                new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
//        extendedMetadataDelegate.setMetadataTrustCheck(true);
//        extendedMetadataDelegate.setMetadataRequireSignature(false);
//        return extendedMetadataDelegate;
//    }
//
//
//    @Bean
//    @Qualifier("idp-okta")
//    public ExtendedMetadataDelegate oktaWoodfordExtendedMetadataProvider()
//            throws MetadataProviderException {
//        String idpOktaWoodfordMetadataURL = "http://woodford-secure.funnelback.co.uk/app/exk9ib1diuU9KPR1l0h7/sso/saml/metadata";
//        Timer backgroundTaskTimer = new Timer(true);
//        HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
//                backgroundTaskTimer, httpClient(), idpOktaWoodfordMetadataURL);
//        httpMetadataProvider.setParserPool(parserPool());
//        ExtendedMetadataDelegate extendedMetadataDelegate =
//                new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
//        extendedMetadataDelegate.setMetadataTrustCheck(true);
//        extendedMetadataDelegate.setMetadataRequireSignature(false);
//        return extendedMetadataDelegate;
//    }

    @Bean
    @Qualifier("idp-mujina")
    public ExtendedMetadataDelegate mujinaExtendedMetadataProvider()
            throws MetadataProviderException {
        String mujinaMetadataURL = System.getProperty("mujinaBaseUrl", "http://localhost:8080") + "/metadata";
        Timer backgroundTaskTimer = new Timer(true);
        HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
                backgroundTaskTimer, httpClient(), mujinaMetadataURL);
        httpMetadataProvider.setParserPool(parserPool());
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
 
  //  // The filter is waiting for connections on URL suffixed with filterSuffix
    // and presents SP metadata there
    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new MetadataDisplayFilter();
    }
     
    // Handler deciding where to redirect user after successful login
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        successRedirectHandler.setDefaultTargetUrl("/search.html");
        return successRedirectHandler;
    }
    
    // Handler deciding where to redirect user after failed login
    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler =
                new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/error");
        return failureHandler;
    }
     
    @Bean
    public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
        SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
        samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOHoKProcessingFilter;
    }
    
    // Processing filter for WebSSO profile messages
    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOProcessingFilter;
    }
     
    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        return new MetadataGeneratorFilter(metadataGenerator());
    }
     
    // Handler for successful logout
    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        successLogoutHandler.setDefaultTargetUrl("/");
        return successLogoutHandler;
    }
     
    // Logout handler terminating local session
    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = 
                new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }
 
    // Filter processing incoming logout messages
    // First argument determines URL user will be redirected to after successful
    // global logout
    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(),
                logoutHandler());
    }
     
    // Overrides default logout processing filter with the one processing SAML
    // messages
    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(),
                new LogoutHandler[] { logoutHandler() },
                new LogoutHandler[] { logoutHandler() });
    }
    
    // Bindings
    private ArtifactResolutionProfile artifactResolutionProfile() {
        final ArtifactResolutionProfileImpl artifactResolutionProfile = 
                new ArtifactResolutionProfileImpl(httpClient());
        artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding()));
        return artifactResolutionProfile;
    }
    
    @Bean
    public HTTPArtifactBinding artifactBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
        return new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile());
    }
 
    @Bean
    public HTTPSOAP11Binding soapBinding() {
        return new HTTPSOAP11Binding(parserPool());
    }
    
    @Bean
    public HTTPPostBinding httpPostBinding() {
        return new HTTPPostBinding(parserPool(), velocityEngine());
    }
    
    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
        return new HTTPRedirectDeflateBinding(parserPool());
    }
    
    @Bean
    public HTTPSOAP11Binding httpSOAP11Binding() {
        return new HTTPSOAP11Binding(parserPool());
    }
    
    @Bean
    public HTTPPAOS11Binding httpPAOS11Binding() {
        return new HTTPPAOS11Binding(parserPool());
    }
    
    // Processor
    @Bean
    public SAMLProcessorImpl processor() {
        Collection<SAMLBinding> bindings = new ArrayList<SAMLBinding>();
        bindings.add(httpRedirectDeflateBinding());
        bindings.add(httpPostBinding());
        bindings.add(artifactBinding(parserPool(), velocityEngine()));
        bindings.add(httpSOAP11Binding());
        bindings.add(httpPAOS11Binding());
        return new SAMLProcessorImpl(bindings);
    }
    
    /**
     * Define the security filter chain in order to support SSO Auth by using SAML 2.0
     * 
     * @return Filter chain proxy
     * @throws Exception
     */
    @Bean
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"),
                samlEntryPoint()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"),
                samlLogoutFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
                metadataDisplayFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
                samlWebSSOProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSOHoK/**"),
                samlWebSSOHoKProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
                samlLogoutProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"),
                samlIDPDiscovery()));
        return new FilterChainProxy(chains);
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
     
    /**
     * Defines the web based security configuration.
     * 
     * @param   http It allows configuring web based security for specific http requests.
     * @throws  Exception 
     */
    @Override  
    protected void configure(HttpSecurity http) throws Exception {
        // New SAML stuff
        http
            .httpBasic()
                .authenticationEntryPoint(samlEntryPoint());
        http
            .csrf()
                .disable();
        http
            .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
            .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class);
        http        
            .authorizeRequests()
            .antMatchers("/").permitAll()
            .antMatchers("/error").permitAll()
            .antMatchers("/saml/**").permitAll()
            .anyRequest().authenticated();
        http
            .logout()
                .logoutSuccessUrl("/");
 
        // OLD stuff - need to integrate this up
//        boolean requireX509Authentication = configRepository.getGlobalConfiguration().valueAsBoolean(Keys.Auth.PublicUI.REQUIRE_X509, false);
//
//        if (requireX509Authentication) {
//            http
//                .authorizeRequests().anyRequest().authenticated()
//                .and()
//                .x509().userDetailsService(new X509UserDetailsService());
//        } else {
//            switch (executionContextHolder.getExecutionContext()) {
//            case Admin:
//                super.configureHttpbasicAndToken(http);
//                break;
//            case Novell:
//                break;
//            case Public:
//                http.authorizeRequests().anyRequest().permitAll();
//                break;
//            case Unknown:
//                break;
//            default:
//                break;
//            }
//        }
//        
//        // Disable csrf, as we don't care to protect anything here with it.
//        http.csrf().disable();
//
//        // This is usually done in the parent however as we don't actually
//        // call the parent to protect this end point when in non Admin mode
//        // we need to ensure this is disabled here.
//        http.headers().httpStrictTransportSecurity().disable();
    }

    /**
     * Sets a custom authentication provider.
     * 
     * @param   auth SecurityBuilder used to create an AuthenticationManager.
     * @throws  Exception 
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .authenticationProvider(samlAuthenticationProvider());
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
