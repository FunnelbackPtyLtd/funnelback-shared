package __fixed_package__;

import com.funnelback.plugin.details.model.*;
import com.funnelback.plugin.docs.model.*;
import com.funnelback.plugin.PluginUtilsBase;

import java.util.List;

public class PluginUtils implements PluginUtilsBase {

    /**
     *   Example key of type String:
     *   PluginConfigKey builder accepts all values needed to create a key such as:
     *   PluginId: Id for current plugin
     *   id: Key Id
     *   type: Type of key, options for this are enlisted here {@link PluginConfigKeyType}
     *   default value: defines default value for key
     *   allowed value: defines range of allowed values
     *   label: key label
     *   description: key description
     *   required: accepts true/false
     *   showIfKeyHasValue: this parameter defines conditional usage of plugin configuration key based on other key's value,
     *                      for details see {@link PluginConfigKeyConditional}
     *
     *   With above configuration, the created key name will be: plugin.__fixed_package__.config.list. This name can be used in configUI to set values.
     *
     *   For details see {@link PluginConfigKey}
     */
    public final PluginConfigKey<List<String>> LIST_KEY = PluginConfigKey.<List<String>>builder()
            .pluginId(getPluginId())
            .id("list")
            .type(PluginConfigKeyType.builder().type(PluginConfigKeyType.Format.ARRAY).subtype(PluginConfigKeyType.Format.STRING).build())
            .defaultValue(List.of())
            .label("List key")
            .description("Define a list of strings")
            .build();

    /**
     *   Example of plugin configuration file.
     *
     *   If a file is needed to define config rules it can be defined using PluginConfigFile properties and applied while implementing the plugin logic.
     *   Please note that validation check for file format needs to be handled by plugin developer.
     *   Also there is no option of checking file format while uploading it. Validations can be done only while reading the file.
     *
     *   For details see {@link PluginConfigFile}
     **/
    public final PluginConfigFile RULES_FILE = PluginConfigFile.builder()
            .name("config-rules.cfg")
            .format("json")
            .label("Config file")
            .description("List of rules to gather data")
            .build();

    /**
     *  Returns list of Configuration keys defined for plugin
     *   All configuration keys defined above need to be included in the list to ensure that they can be configured via UI
     */
    @Override public List <PluginConfigKeyDetails> getConfigKeys() {
        return List.of(LIST_KEY);
    }

    /**
     *  Returns list of Configuration files defined for plugin
     *  All configuration files defined above need to be included in the list to ensure that they can be uploaded via UI
     */
    @Override public List <PluginConfigFile> getConfigFiles() {
        return List.of(RULES_FILE);
    }

    /**
      *  Audience field is used to flag the content of the documentation page as being suitable for one or more of these DXP audiences.
      *  There are different types of Audience defined here {@link Audience}
      *  Please choose appropriate option from the available list.
      *  Most widely used option is site builder, others can be set while developing a plugin on need basis.
      *
      *  This method returns list of Audience selected for the plugin.
     */
    @Override public List <Audience> getAudience() {
        return List.of(Audience.SITE_BUILDER);
    }

    /**
     *  Marketplace subtype is mostly defined by the interfaces that are implemented for the plugin.
     *  Complete list if marketplace subtypes is defined here {@link MarketplaceSubtype}
     */
    @Override public List <MarketplaceSubtype> getMarketplaceSubtype() {
        return List.of(MarketplaceSubtype.GATHERER);
    }

    /**
     *   Product topic is about describing the topics that the plugin you’re building relates to.
     *   So when you select one of those topics in the facets for example the plugin will come up in the possible results.
     *   Available options can be found here {@link ProductTopic}
     */
    @Override public List <ProductTopic> getProductTopic() {
        return List.of(ProductTopic.DATA_SOURCES, ProductTopic.INTEGRATION_DEVELOPMENT);
    }

    /**
     *   Product subtopics are defined for each of the product topic.
     *   Available options can be found here {@link ProductSubtopicCategory}
     */
    @Override public List <ProductSubtopicCategory> getProductSubtopic() {
        return List.of(ProductSubtopic.DataSources.CUSTOM, ProductSubtopic.IntegrationDevelopment.PERFORMANCE);
    }

    /**
     *    Returns plugin ID which should match artifactId from pom.xml
     */
    @Override public String getPluginId() {
        return "${artifactId}";
    }

    /**
     *    Returns plugin name which should match plugin-name from pom.xml
     */
    @Override public String getPluginName() {
        return "${plugin-name}";
    }

    /**
     *    Returns plugin description which should match plugin-description from pom.xml
     */
    @Override public String getPluginDescription() {
        return "${plugin-description}";
    }

    /**
     *   Plugin target can be set here - it indicates if the plugin runs on a data source or a results page (or both)
     *   Available options can be found here {@link PluginTarget}
     **/
    @Override public List <PluginTarget> getPluginTarget() {
        return List.of(__plugin_target__);
    }

    @Override public String getFilterClass() {
        return __plugin_filterClass__;
    }

    @Override public String getJsoupFilterClass() {
        return __plugin_jsoupFilterClass__;
    }

}
