package com.funnelback.publicui.security;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.eclipse.jetty.util.security.Password;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.metadata.provider.AbstractMetadataProvider;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HTTPOutTransport;
import org.opensaml.ws.transport.http.HTTPTransportUtils;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.springmvc.api.config.security.saml.RequestIsAPICallDecider;
import com.funnelback.springmvc.api.config.security.saml.SamlConfig;

@Configuration
@Primary
@Conditional(IsSamlEnabledCondition.class)
public class PublicUISamlConfig {

    @Autowired
    private ConfigRepository configRepository;

    /**
     * Defines how we create a User object from the authentication info the SAML
     * Identity Provider (IdP) provides to us.
     */
    @Bean
    @Primary
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(new SAMLUserDetailsService() {
            public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
                String userID = credential.getNameID().getValue();

                // For publicui we don't authorise at all, so it's sufficient to
                // just provide a user object - We include the username in case
                // it proves useful to later processing (e.g. DLS)
                return new User(userID, "N/A", Arrays.asList(() -> "ROLE_SAML"));
            }
        });
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    /**
     * Defines where we store our private key and certificate so that the Identity Provider (IdP)
     * can validate our traffic.
     * 
     * See KeyManager doc in section 8.1 of
     * http://docs.spring.io/spring-security-saml/docs/current/reference/html/security.html
     */
    @Bean
    @Primary
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
        Resource samlKeystoreResource = loader.getResource("file:" + samlKeystorePath);

        Map<String, String> passwords = new HashMap<String, String>();
        passwords.put(samlKeyAlias, samlKeyPassword);
        // It's unclear to me why we'd ever want to provide
        // keys for other passwords here - Hopefully I'll
        // find out before we ship if there's a good one.
        
        return new JKSKeyManager(samlKeystoreResource, samlKeystorePassword, passwords, samlKeyAlias);
    }
 
    /**
     * Loads the Identity Provider (IdP) metadata (which tells us how to do the SAML login, how to confirm it's them etc)
     */
    @Bean
    @Primary
    public ExtendedMetadataDelegate extendedMetadataProvider(ExtendedMetadata extendedMetadata)
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
                new ExtendedMetadataDelegate(metadataProvider, extendedMetadata);

        extendedMetadataDelegate.initialize();

        return extendedMetadataDelegate;
    }
 
    /**
     * Generates our Service Provider (SP) metadata (allowing the remote Identity Provider (IdP) to understand us)
     */
    @Bean
    @Primary
    public MetadataGenerator metadataGenerator(ExtendedMetadata extendedMetadata, KeyManager keyManager) {
        MetadataGenerator metadataGenerator = new MetadataGenerator();

        String entityId = configRepository.getGlobalConfiguration().value(Keys.Auth.PublicUI.SAML.ENTITY_ID, "");;
        metadataGenerator.setEntityId(entityId);
        metadataGenerator.setExtendedMetadata(extendedMetadata);
        metadataGenerator.setKeyManager(keyManager); 
        return metadataGenerator;
    }

    /* 
     * --- Hacks to allow us to make SAML return a redirect in JSON rather than a 302 ---
     */

    /**
     * A header which allows the caller to force an 'API style' JSON with a body defining the redirect
     * SAML response rather than the normal 302 redirect.
     * 
     * This is required because Ajax calls don't handle redirects on API-like requests from Angular
     * in a way we can detect and handle, so instead we want to return a 401 with a JSON body
     * which indicates where the JavaScript ought to redirect the user.
     */
    public static final String FORCE_SAML_API_AUTHENTICATION_MODE_HEADER = "X-Funnelback-Force-SAML-API-Authentication-Mode";

    /**
     * An optional interface web applications may implement to change how what is an API call and what is not
     * is determined 
     */
    @Autowired(required=false)
    private RequestIsAPICallDecider requestIsAPICallDecider;
    
    @Bean
    @Primary
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding(ParserPool parserPool) {
        return new HTTPRedirectDeflateBinding(new HTTPRedirectDeflateDecoder(parserPool), new FunnelbackHTTPRedirectDeflateEncoder());
    }
    
    private class FunnelbackHTTPRedirectDeflateEncoder extends HTTPRedirectDeflateEncoder {
        private final Logger log = LoggerFactory.getLogger(HTTPRedirectDeflateEncoder.class);
        
        /** {@inheritDoc} */
        // This is the very hacky-part - we duplicate the implementation of doEncode from
        // the superclass and modify the last few lines - Obviously that's fragile to the
        // parent class changing its implementation, so there's a good chance this might
        // break if/when we upgrade spring-security-saml.
        protected void doEncode(MessageContext messageContext) throws MessageEncodingException {
            if (!(messageContext instanceof SAMLMessageContext)) {
                log.error("Invalid message context type, this encoder only support SAMLMessageContext");
                throw new MessageEncodingException(
                        "Invalid message context type, this encoder only support SAMLMessageContext");
            }

            if (!(messageContext.getOutboundMessageTransport() instanceof HTTPOutTransport)) {
                log.error("Invalid outbound message transport type, this encoder only support HTTPOutTransport");
                throw new MessageEncodingException(
                        "Invalid outbound message transport type, this encoder only support HTTPOutTransport");
            }

            SAMLMessageContext samlMsgCtx = (SAMLMessageContext) messageContext;

            String endpointURL = getEndpointURL(samlMsgCtx).buildURL();

            setResponseDestination(samlMsgCtx.getOutboundSAMLMessage(), endpointURL);

            removeSignature(samlMsgCtx);

            String encodedMessage = deflateAndBase64Encode(samlMsgCtx.getOutboundSAMLMessage());

            String redirectURL = buildRedirectURL(samlMsgCtx, endpointURL, encodedMessage);

            HTTPOutTransport out = (HTTPOutTransport) messageContext.getOutboundMessageTransport();
            HTTPTransportUtils.addNoCacheHeaders(out);
            HTTPTransportUtils.setUTF8Encoding(out);
            
            // Our modifications to this method start from here...
            
            HttpServletRequest request = ((HttpServletRequestAdapter) messageContext.getInboundMessageTransport()).getWrappedRequest();
            
            boolean isApiCall;
            if (requestIsAPICallDecider == null) {
                // Normal case - Just respect the headers
                isApiCall = (request.getHeader(SamlConfig.FORCE_SAML_API_AUTHENTICATION_MODE_HEADER) != null);
            } else {
                // if the web app provided a decider, use it instead
                isApiCall = requestIsAPICallDecider.isApiCall(request);
            }
            
            if (isApiCall) {
                // Note - We're violating the rfc here by not providing an Authenticate header
                // because we don't want HTTP Basic authentication. If there's a better status
                // code to use, we don't (today) know what it is :)
                out.setStatusCode(401);
                String message = "{\n"
                    + "    \"errorMessage\" : \"Forbidden, please authenticate.\",\n"
                    + "    \"data\" : {\n"
                    + "        \"samlRedirectTarget\" : \"" + redirectURL + "\"\n"
                    + "    }\n"
                    + "}\n";
                try {
                    out.getOutgoingStream().write( message.getBytes() );
                } catch (IOException e) {
                    throw new MessageEncodingException("Exception writing SAML redirect JSON to response.", e);
                }
            } else {
                out.sendRedirect(redirectURL);
            }
        }
    }

}