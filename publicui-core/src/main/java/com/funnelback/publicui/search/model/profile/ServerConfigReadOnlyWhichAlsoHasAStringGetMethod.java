package com.funnelback.publicui.search.model.profile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.funnelback.config.configtypes.service.ServiceConfigOptionDefinition;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.generic.GenericConfigReadCalls;
import com.funnelback.config.keys.AllKeysMap;
import com.funnelback.config.level.ConfigLevels;
import com.funnelback.config.marshallers.Marshaller;
import com.funnelback.config.marshallers.Marshallers;
import com.funnelback.config.option.ConfigOptionDefinition;
import com.funnelback.config.validators.ConfigOptionValidationException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class ServerConfigReadOnlyWhichAlsoHasAStringGetMethod implements ServiceConfigReadOnly {
    @NonNull private final ServiceConfigReadOnly underylingServiceConfigReadOnly;

    @Override
    public <T> T get(ServiceConfigOptionDefinition<T> configOption) {
        return underylingServiceConfigReadOnly.get(configOption);
    }

    @Override
    public Set<String> getRawKeys() {
        return underylingServiceConfigReadOnly.getRawKeys();
    }

    public String get(String key) {
        ServiceConfigOptionDefinition<Object> option = (ServiceConfigOptionDefinition<Object>) getConfigOptionDefinition(key);
        Object value = get(option);
        if (value == null) {
            return null;
        }
        return option.getMarshaller().marshal(value);
    }

    @Setter(AccessLevel.PACKAGE) private AllKeysMap allKeysMap = new AllKeysMap();
    
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
        //Try to see if the key is already known, if it is use the newest name for that key.
        ServiceConfigOptionDefinition<?> keyToUse = Optional.ofNullable((ServiceConfigOptionDefinition)allKeysMap.getMap().get(name))
                .orElse(new StringKey(name, Optional.empty()));
        if(!name.equals(keyToUse.getKey())) {
            log.debug("The key {} is deprecated, will automatically use {}", name, keyToUse.getKey());
        }
        return keyToUse;
    }
    
    @AllArgsConstructor
    public class StringKey implements ServiceConfigOptionDefinition<String> {
        
        @Getter public String key;
        @Getter public Optional<ConfigOptionDefinition<String>> oldOption;
        
        @Override
        public void validate(GenericConfigReadCalls config, String value) throws ConfigOptionValidationException {
            //Always valid
        }

        @Override
        public Marshaller<String> getMarshaller() {
            return Marshallers.STRING_MARSHALLER;
        }

        @Override
        public String getDefault(GenericConfigReadCalls config) throws IllegalStateException {
            //Default is null.
            return null;
        }

        @Override
        public List<ConfigLevels> getConfigLevels() {
            //Support all configs.
            return Arrays.asList(ConfigLevels.values());
        }
    }

    

}
