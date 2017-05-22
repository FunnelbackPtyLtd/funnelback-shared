package com.funnelback.publicui.security;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.common.config.Keys;

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

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return new GlobalOnlyConfig(new File(System.getProperty(Config.SYSPROP_INSTALL_DIR))).valueAsBoolean(Keys.Auth.PublicUI.SAML.ENABLED);
    }

}
