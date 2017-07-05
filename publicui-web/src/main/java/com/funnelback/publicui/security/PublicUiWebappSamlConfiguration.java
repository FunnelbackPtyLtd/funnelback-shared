package com.funnelback.publicui.security;

import java.io.File;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.funnelback.config.configtypes.server.DefaultServerConfigReadOnly;
import com.funnelback.config.data.file.server.FileServerConfigDataReadOnly;
import com.funnelback.config.keys.Keys.ServerKeys;
import com.funnelback.config.types.ConfigPassword;
import com.funnelback.springmvc.api.config.security.saml.WebappSamlConfiguration;

@Component
@Primary // We want to win over the default, admin configuration
public class PublicUiWebappSamlConfiguration implements WebappSamlConfiguration {

    private DefaultServerConfigReadOnly config;
    
    @Autowired
    public PublicUiWebappSamlConfiguration(File searchHome) {
        config = new DefaultServerConfigReadOnly(new FileServerConfigDataReadOnly(searchHome));
    }
    
    @Override
    public Optional<File> groovySamlPermissionMapperFile() {
        return Optional.empty();
    }

    @Override
    public Optional<File> getKeystoreFile() {
        return config.get(ServerKeys.Auth.PublicUI.SAML.KEYSTORE_PATH);
    }

    @Override
    public Optional<ConfigPassword> getKeystorePassword() {
        return config.get(ServerKeys.Auth.PublicUI.SAML.MANAGER_PASSWORD);
    }

    @Override
    public Optional<String> getKeyAlias() {
        return config.get(ServerKeys.Auth.PublicUI.SAML.KEY_ALIAS);
    }

    @Override
    public Optional<ConfigPassword> getKeyPassword() {
        return config.get(ServerKeys.Auth.PublicUI.SAML.KEY_PASSWORD);
    }

    @Override
    public Optional<String> getIdentityProviderUrl() {
        return config.get(ServerKeys.Auth.PublicUI.SAML.IDENTITY_PROVIDER_METADATA_URL);
    }

    @Override
    public Optional<String> getEntityId() {
        return config.get(ServerKeys.Auth.PublicUI.SAML.ENTITY_ID);
    }

}
