package com.funnelback.plugin.details.model.deserializer;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.funnelback.plugin.details.model.PluginConfigKey;
import com.funnelback.plugin.details.model.PluginConfigKeyAllowedValue;
import com.funnelback.plugin.details.model.PluginConfigKeyConditional;
import com.funnelback.plugin.details.model.PluginConfigKeyDetails;
import com.funnelback.plugin.details.model.PluginConfigKeyEncrypted;
import com.funnelback.plugin.details.model.PluginConfigKeyType;
/**
 * This class is used to deserialize objects which implement the PluginConfigKeyDetails class
 * It is required because during the serialization process a pluginId and id are converted to the "key" JsonProperty
 */
@JsonPOJOBuilder(buildMethodName = "createBean", withPrefix = "construct")
public class PluginConfigKeyDetailsDeserializer<T> {

    private String key;
    private String id;
    private PluginConfigKeyAllowedValue allowedValue;
    private String label;
    private String description;
    private boolean required;
    private PluginConfigKeyType type;
    private T defaultValue;
    private PluginConfigKeyConditional showIfKeyHasValue;

    /**
     *
     * @param key Value from Deserialized JSON used in the createBean method.
     */
    public PluginConfigKeyDetailsDeserializer constructKey(String key) {
        this.key = key;
        return this;
    }

    public PluginConfigKeyDetailsDeserializer constructId(String id) {
        this.id = id;
        return this;
    }

    public PluginConfigKeyDetailsDeserializer constructAllowedValue(PluginConfigKeyAllowedValue allowedValue) {
        this.allowedValue = allowedValue;
        return this;
    }

    public PluginConfigKeyDetailsDeserializer constructDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public PluginConfigKeyDetailsDeserializer constructDescription(String description) {
        this.description = description;
        return this;
    }

    public PluginConfigKeyDetailsDeserializer constructLabel(String label) {
        this.label = label;
        return this;
    }

    public PluginConfigKeyDetailsDeserializer constructRequired(boolean required) {
        this.required = required;
        return this;
    }

    public PluginConfigKeyDetailsDeserializer constructShowIfKeyHasValue(PluginConfigKeyConditional showIfKeyHasValue) {
        this.showIfKeyHasValue = showIfKeyHasValue;
        return this;
    }

    public PluginConfigKeyDetailsDeserializer constructType(PluginConfigKeyType type) {
        this.type = type;
        return this;
    }

    /**
     * The createBean method is executed after the Json object has been deserialized into the above properties.
     * This method inspects the key property that has been deserialized and decides the pluginId and return class.
     */
    public PluginConfigKeyDetails createBean() {
        String keyStripped = key.substring(PluginConfigKey.PLUGIN_PREFIX.length(), key.length() - id.length());

        // Check if the configuration item is a standard config item and if so create the appropriate PluginConfigKeyDetails
        if (keyStripped.endsWith(PluginConfigKeyDetails.PLUGIN_CONFIG_QUALIFIER)) {
            String configPluginId = keyStripped.substring(0, keyStripped.length() - PluginConfigKeyDetails.PLUGIN_CONFIG_QUALIFIER.length());
            return PluginConfigKey.builder()
                    .allowedValue(allowedValue)
                    .defaultValue(defaultValue)
                    .pluginId(configPluginId)
                    .id(id)
                    .label(label)
                    .description(description)
                    .required(required)
                    .type(type)
                    .showIfKeyHasValue(showIfKeyHasValue)
                    .build();
        }

        // Check if the configuration item is a standard config item and if so create the appropriate PluginConfigKeyDetails
        if (keyStripped.endsWith(PluginConfigKeyDetails.PLUGIN_ENCRYPTED_QUALIFIER)) {
            String encryptedPluginId = keyStripped.substring(0, keyStripped.length() - PluginConfigKeyDetails.PLUGIN_ENCRYPTED_QUALIFIER.length());
            return PluginConfigKeyEncrypted.builder()
                    .pluginId(encryptedPluginId)
                    .id(id)
                    .label(label)
                    .description(description)
                    .required(required)
                    .build();
        }

        // If the type of configuration item cant be determined throw error
        throw new RuntimeException("Unable to Deserialize JSON schema as the pluginId can't be determined");
    }
}
