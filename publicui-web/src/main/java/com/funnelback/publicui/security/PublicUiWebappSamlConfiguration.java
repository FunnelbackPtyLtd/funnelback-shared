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
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.api.config.security.saml.WebappSamlConfiguration;

@Component
@Primary // We want to win over the default, admin configuration
public class PublicUiWebappSamlConfiguration implements WebappSamlConfiguration {

    private static final String PUBLIC_UI_SAML_SUFFIX = ":publicui:sp";

    private DefaultServerConfigReadOnly config;

    private ExecutionContextHolder executionContextHolder;

    @Autowired
    public PublicUiWebappSamlConfiguration(File searchHome, ExecutionContextHolder executionContextHolder) {
        config = new DefaultServerConfigReadOnly(new FileServerConfigDataReadOnly(searchHome));
        this.executionContextHolder = executionContextHolder;
    }

    @Override
    public Optional<File> groovySamlPermissionMapperFile() {
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            return config.get(ServerKeys.Auth.Admin.SAML.GROOVY_PERMISSION_MAPPER);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> getKeystoreFile() {
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            return config.get(ServerKeys.Auth.Admin.SAML.KEYSTORE_PATH);
        } else {
            return config.get(ServerKeys.Auth.PublicUI.SAML.KEYSTORE_PATH);
        }
    }

    @Override
    public Optional<ConfigPassword> getKeystorePassword() {
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            return config.get(ServerKeys.Auth.Admin.SAML.MANAGER_PASSWORD);
        } else {
            return config.get(ServerKeys.Auth.PublicUI.SAML.MANAGER_PASSWORD);
        }
    }

    @Override
    public Optional<String> getKeyAlias() {
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            return config.get(ServerKeys.Auth.Admin.SAML.KEY_ALIAS);
        } else {
            return config.get(ServerKeys.Auth.PublicUI.SAML.KEY_ALIAS);
        }
    }

    @Override
    public Optional<ConfigPassword> getKeyPassword() {
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            return config.get(ServerKeys.Auth.Admin.SAML.KEY_PASSWORD);
        } else {
            return config.get(ServerKeys.Auth.PublicUI.SAML.KEY_PASSWORD);
        }
    }

    @Override
    public Optional<String> getIdentityProviderUrl() {
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            return config.get(ServerKeys.Auth.Admin.SAML.IDENTITY_PROVIDER_METADATA_URL);
        } else {
            return config.get(ServerKeys.Auth.PublicUI.SAML.IDENTITY_PROVIDER_METADATA_URL);
        }
    }

    @Override
    public Optional<String> getEntityId() {
        Optional<String> cfgValue;
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            cfgValue = config.get(ServerKeys.Auth.Admin.SAML.ENTITY_ID_PREFIX);
        } else {
            cfgValue = config.get(ServerKeys.Auth.PublicUI.SAML.ENTITY_ID_PREFIX);
        }
        return cfgValue.map(value -> value + PUBLIC_UI_SAML_SUFFIX);
    }

}
