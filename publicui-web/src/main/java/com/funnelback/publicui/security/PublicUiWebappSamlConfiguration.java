package com.funnelback.publicui.security;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.funnelback.config.configtypes.server.DefaultServerConfigReadOnly;
import com.funnelback.config.configtypes.server.ServerConfigOptionDefinition;
import com.funnelback.config.data.file.server.FileServerConfigDataReadOnly;
import com.funnelback.config.keys.Keys.ServerKeys;
import com.funnelback.config.types.ConfigPassword;
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.api.config.security.saml.WebappSamlConfiguration;

@Component
@Primary // We want to win over the default, admin configuration
public class PublicUiWebappSamlConfiguration implements WebappSamlConfiguration {

    private DefaultServerConfigReadOnly config;
    private ServletContext servletContext;

    private ExecutionContextHolder executionContextHolder;

    @Autowired
    public PublicUiWebappSamlConfiguration(File searchHome, ServletContext servletContext, ExecutionContextHolder executionContextHolder) {
        config = new DefaultServerConfigReadOnly(new FileServerConfigDataReadOnly(searchHome));
        this.executionContextHolder = executionContextHolder;
        this.servletContext = servletContext;
    }

    @Override
    public Optional<File> groovySamlPermissionMapperFile() {
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            Optional<File> result = config.get(ServerKeys.Auth.Admin.SAML.GROOVY_PERMISSION_MAPPER);
            
            if (!result.isPresent()) {
                throw new RuntimeException("SAML Authentication is enabled, but no " + ServerKeys.Auth.Admin.SAML.GROOVY_PERMISSION_MAPPER.getKey() + " setting was given.");
            }
            
            return result;
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
        ServerConfigOptionDefinition<Optional<String>> key;
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            key = ServerKeys.Auth.Admin.SAML.ENTITY_ID_PREFIX;
        } else {
            key = ServerKeys.Auth.PublicUI.SAML.ENTITY_ID_PREFIX;
        }

        return config.get(key).map((prefix) -> {
            String fullEntityId = prefix + servletContext.getContextPath();
            try {
                return new URI(fullEntityId).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid SAML entity ID - " + fullEntityId + " based on global.cfg's "
                    + key.getKey() + " was expected to be a valid URI", e);
            }
        });
    }

    @Override
    public String getEntityBaseURL() {
        if (ExecutionContext.Admin.equals(executionContextHolder.getExecutionContext())) {
            return config.get(ServerKeys.Urls.ADMIN_PROTOCOL) + "://" + config.get(ServerKeys.Urls.ADMIN_HOSTNAME) + ":"
                + config.get(ServerKeys.Urls.ADMIN_PORT) + servletContext.getContextPath();
        } else {
            return config.get(ServerKeys.Urls.SEARCH_PROTOCOL) + "://" + config.get(ServerKeys.Urls.SEARCH_HOSTNAME) + ":"
                + config.get(ServerKeys.Urls.SEARCH_PORT) + servletContext.getContextPath();
        }
    }

}
