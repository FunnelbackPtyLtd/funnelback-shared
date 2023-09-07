package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.*;
import com.funnelback.plugin.docs.model.Audience;
import com.funnelback.plugin.docs.model.MarketplaceSubtype;
import com.funnelback.plugin.docs.model.ProductSubtopicCategory;
import com.funnelback.plugin.docs.model.ProductTopic;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AsciiDocGenerator {
    final static String FILE_NAME = "index.adoc";
    final static String DOCUMENTATION = "Documentation";
    final static String PLUGINS = "Plugins";
    final static String KEYWORDS = "keywords.adoc";
    final static String ADDITIONALCONFIGSETTINGS = "additionalconfigsettings.adoc";
    final static String DETAILEDDESCRIPTION = "detaileddescription.adoc";
    final static String CONFIGFILE = "configfile_";
    final static String CHANGELOG = "changelog.adoc";
    final static String EXAMPLE = "example.adoc";
    final static String SEEALSOLINKS = "seealsolinks.adoc";
    private final PluginUtilsBase pluginUtils;
    private final String resourcesPath;
    private final String projectResourcePath;
    private final String packageName;
    private final String projectVersion;
    private String pluginTargets;
    private StringBuilder content = new StringBuilder();
    AsciiDocGenerator(PluginUtilsBase pluginUtils, String resourcesPath, String packageName, String projectVersion, String projectResourcePath) {
        this.pluginUtils = pluginUtils;
        this.resourcesPath = resourcesPath;
        this.packageName = packageName;
        this.projectVersion = projectVersion;
        this.projectResourcePath = projectResourcePath + "/ascii/sections/";
    }

    public String generateASCIIDocument()  {
        try {
            getPluginTargets();
            // This section appends all metadata tags for the plugin
            content.append("= Plugin: ").append(pluginUtils.getPluginName());
            content.append("\n:page-description: ").append(pluginUtils.getPluginDescription());
            content.append("\n:page-keywords: ");
            String result = readAsciiFile(projectResourcePath + KEYWORDS);
            if (!result.isEmpty()) {
                int lastIndex = result.lastIndexOf("\n");
                content.append(result.replace("\n",","), 0, lastIndex);
            }
            content.append("\n:page-content-type: ").append(StringUtils.join(DOCUMENTATION, "|", PLUGINS));
            content.append("\n:page-hc-audience: ").append(pluginUtils.getAudience().stream()
                                                        .map(Audience::getType)
                                                        .collect(Collectors.joining("|")));
            content.append("\n:page-marketplace-type: ").append("Plugin");
            content.append("\n:page-marketplace-subtype: ").append(pluginUtils.getMarketplaceSubtype().stream()
                                                                .map(MarketplaceSubtype::getType)
                                                                .collect(Collectors.joining("|")));
            content.append("\n:page-marketplace-version: ").append(projectVersion);
            content.append("\n:page-plugin-scope: ").append(pluginTargets);
            content.append("\n:page-plugin-package: ").append(packageName);
            content.append("\n:page-plugin-id: ").append(pluginUtils.getPluginId());
            content.append("\n:page-plugin-interface: ").append(getPluginInterfaces());
            content.append("\n:page-product-topic: ").append(pluginUtils.getProductTopic().stream()
                                                        .map(ProductTopic::getTopic)
                                                        .collect(Collectors.joining("|")));
            content.append("\n:page-product-subtopic: ").append(pluginUtils.getProductSubtopic().stream()
                                                            .map(ProductSubtopicCategory::getTopic)
                                                            .collect(Collectors.joining("|")));

            addPluginDescription();
            content.append("\n== Usage\n\n");
            content.append("=== Enable the plugin\n\n");
            if ((pluginUtils.getPluginTarget().contains(PluginTarget.DATA_SOURCE)) && (pluginUtils.getPluginTarget().contains(PluginTarget.RESULTS_PAGE))){
                addDataSourceResultsPageConfigurations();
            }
            else if (pluginUtils.getPluginTarget().contains(PluginTarget.DATA_SOURCE)){
                addDataSourceConfigurations();
            }
            else if(pluginUtils.getPluginTarget().contains(PluginTarget.RESULTS_PAGE)){
                addResultsPageConfigurations();
            }

            writePluginConfigKeysToResourceAdoc();

            // Additional configuration settings
            content.append(readAsciiFile(projectResourcePath + ADDITIONALCONFIGSETTINGS));

            if (pluginUtils.getPluginTarget().contains(PluginTarget.DATA_SOURCE)) {
                if ((StringUtils.isNotEmpty(pluginUtils.getFilterClass())) || (StringUtils.isNotEmpty(pluginUtils.getJsoupFilterClass()))) {
                    content.append("\n=== Filter chain configuration\n" +
                            "\n" +
                            "This plugin uses filters which are used to apply transformations to the gathered content.\n" +
                            "\n" +
                            "The filters run in sequence and need be set in an order that makes sense. The plugin supplied filter(s) (as indicated in the listing) should be re-ordered to an appropriate point in the sequence.\n" +
                            "\n" +
                            "WARNING: Changes to the filter order affects the way the data source processes gathered documents. See: xref:build/data-sources/document-filtering/index.adoc[document filters documentation].\n" +
                            "\n");
                    // Document filters
                    if (StringUtils.isNotEmpty(pluginUtils.getFilterClass())) {
                        addDocumentFilter();
                    }
                    // Jsoup filters
                    if (StringUtils.isNotEmpty(pluginUtils.getJsoupFilterClass())) {
                        addJsoupFilter();
                    }
                }

            }
            addConfigFileDetails();
            appendExampleFileContents();

            // Add Change log if it has any content
            String changeLog = readAsciiFile(projectResourcePath + CHANGELOG);
            if (!changeLog.isEmpty()) {
                content.append("\n\n").append("== Change log\n").append(changeLog);
            }
            writeToFile(appendSeeAlso(content), resourcesPath);
            return content.toString();
        }catch (Exception e){
                throw new RuntimeException(e);
        }
    }

    private void getPluginTargets(){
        if (pluginUtils.getPluginTarget().contains(PluginTarget.DATA_SOURCE)){
            this.pluginTargets = PluginTarget.DATA_SOURCE.getTarget().toLowerCase();
        }
        if (pluginUtils.getPluginTarget().contains(PluginTarget.RESULTS_PAGE)){
            this.pluginTargets = StringUtils.join(this.pluginTargets, " or ", PluginTarget.RESULTS_PAGE.getTarget().toLowerCase());
        }
    }

    private void addPluginDescription(){
        content.append("\n\n").append("== Purpose ").append("\n\n");
        content.append(pluginUtils.getPluginDescription()).append("\n\n");
        String description = readAsciiFile(projectResourcePath + DETAILEDDESCRIPTION);
        if (!description.isEmpty()){
            content.append(description).append("\n");
        }
    }

    private void addDataSourceResultsPageConfigurations() {
        // Plugin supports both data sources and results pages
        content.append(". Select menu:Plugins[] from the side navigation pane and click on the *" + pluginUtils.getPluginName() + "* tile.\n" +
                       ". From the *Location* section, decide if you wish to enable this plugin on a *data source* or a *results page* and select the corresponding radio button.\n" +
                       ". Select the data source or results page to which you would like to enable this plugin from the drop-down menu.\n" +
                       "\n" +
                       "NOTE: If enabled on a data source, the plugin will take effect as soon as the setup steps are completed, and an advanced > full update of the data source has completed. If enabled on a results page the plugin will take effect as soon as the setup steps are completed.\n" +
                       "\n");

    }
    private void addDataSourceConfigurations() {

        content.append(". Select menu:Plugins[] from the side navigation pane and click on the *" + pluginUtils.getPluginName() + "* tile.\n" +
                       ". From the *Location* section, select the data source to which you would like to enable this plugin from the _Select a data source_ select list.\n" +
                       "\n");

        content.append("NOTE: The plugin will take effect after setup steps and an advanced > full update of the data source has completed.\n" +
                       "\n");
    }
    private void addResultsPageConfigurations(){
        content.append(". Select menu:Plugins[] from the side navigation pane and click on the *" + pluginUtils.getPluginName() + "* tile.\n" +
                ". From the Location* section, select the results page to which you would like to enable this plugin from the _Select a results page_ select list.\n" +
                "\n" +
                "NOTE: The plugin will take effect as soon as you finish running through the plugin setup steps.\n" +
                "\n");
    }

    private void addDocumentFilter() {
        if (StringUtils.isNotEmpty(pluginUtils.getFilterClass())) {
            // Add note for append it to end of filter
            content.append("==== Filter classes\n" +
                            "\n" +
                            "This plugin supplies a filter that runs in the main document filter chain: `+" + pluginUtils.getFilterClass() +"+`\n" +
                            "\n" +
                            "Drag the *+" + pluginUtils.getFilterClass() + "+* plugin filter to where you wish it to run in the filter chain sequence.\n" +
                            "\n");
        }
    }
    private void addJsoupFilter(){
        if (StringUtils.isNotEmpty(pluginUtils.getJsoupFilterClass())){
            content.append("==== Jsoup filter classes\n" +
                           "\n" +
                           "This plugin supplies a filter that needs to run in the HTML document (Jsoup) filter chain:`+" + pluginUtils.getJsoupFilterClass() + "+`\n" +
                           "\n" +
                           "Drag the *+" + pluginUtils.getJsoupFilterClass() + "+* plugin filter to where you wish it to run in the filter chain sequence.\n" +
                           "\n");
        }
    }


    private void writePluginConfigKeysToResourceAdoc() {
        List<PluginConfigKeyDetails> configKeys = pluginUtils.getConfigKeys();
        if (!configKeys.isEmpty()) {
            content.append("=== Configuration settings\n" +
                           "\n" +
                           "The *configuration settings* section is where you do most of the configuration for your plugin. The settings enable you to control how the plugin behaves.\n" +
                           "\n" +
                           "NOTE: The configuration key names below are only used if you are configuring this plugin manually. The configuration keys are set in the " + pluginTargets + " configuration to configure the plugin. When setting the keys manually you need to type in (or copy and paste) the key name and value.\n" +
                           "\n");

            for (PluginConfigKeyDetails configKey : configKeys) {
                if (configKey instanceof PluginConfigKey) {
                    PluginConfigKey<?> key = (PluginConfigKey<?>) configKey;
                    PluginConfigKeyType keyType = key.getType();
                    String keyTypeText = (keyType.equals(PluginConfigKeyType.Format.ARRAY))
                            ? keyType.getSubtype().getType()
                            : keyType.getType().getType();

                    Object defaultValue = configKey.getDefaultValue();
                    if (defaultValue instanceof List){
                        if (((List<?>) defaultValue).size() == 0){
                            defaultValue = "an empty list";
                        }
                    }

                    PluginConfigKeyAllowedValue allowedValue = configKey.getAllowedValue();

                    content.append("==== " + key.getLabel() + "\n" +
                                  "\n" +
                                  "[%autowidth.spread]\n" +
                                  "|===\n" +
                                  "|Configuration key| `" + key.getKey() +"`\n" +
                                  "|Data type|" + keyTypeText +"\n");
                    if (defaultValue != null) {
                        content.append("|Default value|`+" + defaultValue + "+`\n");
                    }
                    if (allowedValue != null){
                        if (allowedValue.getValues() != null && allowedValue.getValues().size() > 0) {
                            content.append("|Allowed values|" + allowedValue.getValues().stream().map(Object::toString).collect(Collectors.joining(",")) + "\n");
                        }
                        if (allowedValue.getRegex() != null) {
                            content.append("|Value format|Allowed values must match the regular expression:\n" +
                                    "\n" +
                                    "`++" + allowedValue.getRegex().toString() + "++`\n");
                        }
                    }
                    content.append("|===\n" +
                            "\n");
                    content.append(key.getDescription());
                } else if (configKey instanceof PluginConfigKeyEncrypted) {
                    PluginConfigKeyEncrypted encryptedKey = (PluginConfigKeyEncrypted) configKey;
                    content.append("==== " + encryptedKey.getLabel() + "\n" +
                                   "\n" +
                                   "[%autowidth.spread]\n" +
                                   "|===\n" +
                                   "|Configuration key| `" + encryptedKey.getKey() +"`\n" +
                                   "|Data type|Encrypted string\n" +
                                   "|===\n" +
                                   "\n" +
                                   encryptedKey.getDescription());
                }
                content.append("\n\n");
            }
        }
    }

    private void addConfigFileDetails(){
        if (pluginUtils.getConfigFiles() != null && pluginUtils.getConfigFiles().size() > 0){
            content.append("=== Configuration files\n" +
                    "\n" +
                    "This plugin also uses the following configuration files to provide additional configuration.\n" +
                    "\n");
            for (PluginConfigFile file: pluginUtils.getConfigFiles()) {
                File configFileDetailsSource = new File(projectResourcePath + CONFIGFILE + file.getName() + ".adoc");
                content.append("==== " + file.getName() + "\n" +
                               "\n" +
                               "[%autowidth.spread]\n" +
                               "|===\n" +
                               "|Description|" + file.getDescription() + "\n" +
                               "|Configuration file format|" + file.getFormat() + "\n" +
                               "|===\n" +
                               "\n");
                if (configFileDetailsSource.exists()){
                    content.append(readAsciiFile(configFileDetailsSource.getPath())).append("\n\n");
                } else {
                    content.append("WARNING: Details for plugin configuration file `" + file.getName() + "` are not added. Please create the documentation in `/src/test/resources/ascii/sections/" + CONFIGFILE + file.getName() + ".adoc`\n\n");
                }
            }
        }
    }

    private String appendSeeAlso(StringBuilder content){
        content.append("\n== See also\n\n");
        content.append("* xref:build/plugins/index.adoc[Plugins]");
        content.append("\n").append(readAsciiFile(projectResourcePath + SEEALSOLINKS));
        return content.toString();
    }

    // Readme file will talk about how to use pluginutils and how to  use ascii doc
    private void writeToFile( String content, String resourcesPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resourcesPath + "/" + FILE_NAME))) {
            writer.write(content);
            System.out.println("Plugin Documentation generated successfully!");
        } catch (IOException e) {
            System.err.println("Error writing plugin documentation to file: " + e.getMessage());
        }
    }

    private void appendExampleFileContents(){
        if (!new File(projectResourcePath + EXAMPLE).exists()){
            throw new RuntimeException("File '/src/main/resources/ascii/sections/" + EXAMPLE + "' with plugin examples needs to exist for successful compilation of plugin.");
        }
        String blankExample = "// ==========\n" +
                              "// Examples showing how to use this plugin. All plugins should include a minimum of one example. More complex plugins\n" +
                              "// will require a set of plugins.\n" +
                              "//\n" +
                              "// See: https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#examples more details and examples.\n" +
                              "// ==========\n";

        String examples = readAsciiFile(projectResourcePath + EXAMPLE);
        content.append("\n").append("== Examples\n\n");
        if (examples.equals(blankExample) || examples.isEmpty()){
            content.append("WARNING: It is recommended to include at least one example about usage of the plugin.");
        }else {
            content.append(examples);
        }
    }

    private String getPluginInterfaces() {
        List<String> interfacesList = new ArrayList<>();

        List<MarketplaceSubtype> marketplaceSubtypes = pluginUtils.getMarketplaceSubtype();

        for (MarketplaceSubtype subtype : marketplaceSubtypes) {
            switch (subtype) {
                case GATHERER:
                    interfacesList.add("gatherer");
                    break;
                case FILTER:
                    interfacesList.add("filtering");
                    interfacesList.add("jsoup-filtering");
                    break;
                case INDEXING:
                    interfacesList.add("indexing");
                    break;
                case SEARCH_LIFECYCLE:
                    interfacesList.add("searchLifeCycle");
                    break;
                case SERVLET_FILTER:
                    interfacesList.add("searchServletFilterHook");
                    break;
                case FACETED_NAVIGATION_SORT:
                    interfacesList.add("facets");
                    break;
            }
        }
        return String.join("|", interfacesList);
    }
    private String readAsciiFile(String filePath){
        StringBuilder result = new StringBuilder();
        try {
            File file = new File(filePath);
            if (file.exists()){
                result.append(Files.readString(file.toPath())).append("\n");
            }
        }catch (IOException e){
            System.err.println("Cannot read ascii file" + filePath + e.getMessage());
        }
        return result.toString();
    }
}
