package com.funnelback.publicui.security;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.eclipse.jetty.util.security.Password;
import org.opensaml.saml2.metadata.provider.AbstractMetadataProvider;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;

@Configuration
@Conditional(IsSamlEnabledCondition.class)
public class SamlConfig {

    @Autowired
    private ConfigRepository configRepository;

    /**
     * Defines how we create a User object from the authentication info the SAML
     * Identity Provider (IdP) provides to us.
     */
    @Bean
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
 
    /**
     * Loads the Identity Provider (IdP) metadata (which tells us how to do the SAML login, how to confirm it's them etc)
     */
    @Bean
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
    public MetadataGenerator metadataGenerator(ExtendedMetadata extendedMetadata, KeyManager keyManager) {
        MetadataGenerator metadataGenerator = new MetadataGenerator();

        String entityId = configRepository.getGlobalConfiguration().value(Keys.Auth.PublicUI.SAML.ENTITY_ID, "");;
        metadataGenerator.setEntityId(entityId);
        metadataGenerator.setExtendedMetadata(extendedMetadata);
        metadataGenerator.setKeyManager(keyManager); 
        return metadataGenerator;
    }
}