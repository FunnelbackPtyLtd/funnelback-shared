package com.funnelback.publicui.security;

import java.io.File;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.common.config.Keys;

import lombok.Synchronized;

/**
 * Checks if SAML is enabled to allow us to make constructing the SAML beans conditional.
 * 
 * Unfortunately, due to the constraints imposed by Spring, this currently must rely on
 * the funnelback.installdir system property and read global.cfg.
 * 
 * We currently think that's less-bad than having the jetty container create a dedicated
 * property (perhaps a Spring profile), but maybe a better option will be discovered soon. 
 */
public class IsSamlEnabledCondition implements Condition {

    // Tried Spring's @Cachable, but it didn't work - Not sure why
    private static Boolean isEnabled = null;
    
    @Override
    @Synchronized
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (isEnabled == null) {
            GlobalOnlyConfig config = new GlobalOnlyConfig(new File(System.getProperty(Config.SYSPROP_INSTALL_DIR)));
            
            isEnabled = config.valueAsBoolean(Keys.Auth.PublicUI.ENABLE_SAML, false);
        }
        return isEnabled;
    }

}
