package com.funnelback.publicui.search.model.profile;

import java.util.Optional;

import com.funnelback.config.keys.StringKey;
import com.funnelback.config.configtypes.service.ServiceConfigOptionDefinition;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.ConfigKeyFinder;

import lombok.AccessLevel;
import lombok.Delegate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class ServerConfigReadOnlyWhichAlsoHasAStringGetMethod implements ServiceConfigReadOnly {
    @NonNull @Delegate private final ServiceConfigReadOnly underylingServiceConfigReadOnly;

    public String get(String key) {
        ServiceConfigOptionDefinition<Object> option = (ServiceConfigOptionDefinition<Object>) getConfigOptionDefinition(key);
        Object value = get(option);
        if (value == null) {
            return null;
        }
        return option.getMarshaller().marshal(value);
    }

    @Setter(AccessLevel.PACKAGE) private ConfigKeyFinder keyFinder = new ConfigKeyFinder();
    
    /**
     * Creates a ConfigOptionDefintion for a given key.
     * 
     * <p>The resulting ConfigOptionDefintion does nothing special. It does nothing
     * to find what the old or new keys look like, and makes no effort to upgrade the
     * values.</p> 
     * 
     * @param name
     * @return
     */
    private ServiceConfigOptionDefinition<?> getConfigOptionDefinition(String name) {
        // Try to see if the key is already known, if it is use that key.
        return keyFinder.findCurrentConfigKey(name)
            .map(c -> c.getConfigOption())
            .filter(configOption -> configOption instanceof ServiceConfigOptionDefinition)
            .map(configOption -> (ServiceConfigOptionDefinition) configOption)
            .orElse(new StringKey(name, Optional.empty()));
    }
}
