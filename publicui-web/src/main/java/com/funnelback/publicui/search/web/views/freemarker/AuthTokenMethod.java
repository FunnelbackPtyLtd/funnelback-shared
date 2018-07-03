package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;

import static com.funnelback.config.keys.Keys.ServerKeys;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Generates an authentication token for a given URL
 * based on the {@link Server#SERVER_SECRET};
 * 
 * @since 12.4
 */
public class AuthTokenMethod extends AbstractTemplateMethod {

    public static final String NAME = "authToken";
    
    public AuthTokenMethod() {
        super(1, 0, false);
    }

    @Autowired
    @Setter private ConfigRepository configRepository;
    
    @Autowired
    @Setter private AuthTokenManager authTokenManager;
    
    @Override
    protected Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        String data = ((TemplateScalarModel) arguments.get(0)).getAsString();
        
        return authTokenManager.getToken(data, configRepository.getServerConfig().get(ServerKeys.SERVER_SECRET));
    }

}
