package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.funnelback.common.config.BaseConfig.StringKey;
import com.funnelback.config.configtypes.component.ComponentConfigOptionDefinition;
import com.funnelback.config.configtypes.service.ServiceConfigOptionDefinition;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.generic.GenericConfigReadCalls;
import com.funnelback.config.keys.AllKeysMap;
import com.funnelback.config.level.ConfigLevels;
import com.funnelback.config.marshallers.Marshaller;
import com.funnelback.config.marshallers.Marshallers;
import com.funnelback.config.option.ConfigOptionDefinition;
import com.funnelback.config.validators.ConfigOptionValidationException;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

public class FunnelbackFreemarkerObjectWrapper extends DefaultObjectWrapper {

    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof ServiceConfigReadOnly) {
            System.out.println("About to magically wrap the service confg!");
            return super.wrap(new UntypedServiceConfigReadOnly((ServiceConfigReadOnly)obj));            
        } else {
            return super.wrap(obj);
        }
    }
    
    @RequiredArgsConstructor
    @Log4j2
    public static class UntypedServiceConfigReadOnly implements ServiceConfigReadOnly {
        private final ServiceConfigReadOnly underylingServiceConfigReadOnly;
        
        @Override
        public <T> T get(ComponentConfigOptionDefinition<T> configOption) {
            return underylingServiceConfigReadOnly.get(configOption);
        }

        @Override
        public <T> T get(ServiceConfigOptionDefinition<T> configOption) {
            return underylingServiceConfigReadOnly.get(configOption);
        }

        @Override
        public Set<String> getRawKeys() {
            return underylingServiceConfigReadOnly.getRawKeys();
        }

        public String get(String key) {
            Object result = this.get(this.getConfigOptionDefinition(key));
            return result == null ? "null" : result.toString();
        }

        @Setter(AccessLevel.PACKAGE) public AllKeysMap allKeysMap = new AllKeysMap();
        
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
                //Allways valid
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
}
