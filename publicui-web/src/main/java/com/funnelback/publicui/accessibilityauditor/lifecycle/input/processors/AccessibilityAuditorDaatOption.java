package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import java.util.Optional;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.padre.QueryProcessorOptionKeys;

public class AccessibilityAuditorDaatOption {

    /**
     * Set a higher DAAT value than the default if configured.
     * 
     * @param config Config to read the DAAT value from
     * @return PADRE <code>daat</code> option, or nothing if not configured
     */
    public Optional<String> getDaatOption(Config config) {
        if (config.hasValue(Keys.ModernUI.AccessibilityAuditor.DAAT_LIMIT)) {
            return Optional.of("-" + QueryProcessorOptionKeys.DAAT + "=" + config.value(Keys.ModernUI.AccessibilityAuditor.DAAT_LIMIT));
        } else {
            return Optional.empty();
        }
    }
}
