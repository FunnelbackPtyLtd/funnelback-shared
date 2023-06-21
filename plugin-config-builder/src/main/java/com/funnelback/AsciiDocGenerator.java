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
            content.append("= Plugin: ").append(pluginUtils.getPluginName()).append("\n");
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
            if (pluginUtils.getPluginTarget().contains(PluginTarget.DATA_SOURCE)){
                addDataSourceConfigurations();
            }
            if(pluginUtils.getPluginTarget().contains(PluginTarget.RESULTS_PAGE)){
                addResultsPageConfigurations();
            }
            writePluginConfigKeysToResourceAdoc();
            content.append("\n").append(readAsciiFile(projectResourcePath + ADDITIONALCONFIGSETTINGS));
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

    private void addDataSourceConfigurations(){
        content.append("=== Enable the plugin on data source").append("\n");
        content.append("\nEnable the *").append(pluginUtils.getPluginName()).append("* plugin on your *")
                .append(PluginTarget.DATA_SOURCE.getTarget().toLowerCase()).append("* from the *plugins* screen in the search dashboard or add the following *")
                .append(PluginTarget.DATA_SOURCE.getTarget().toLowerCase())
                .append(" configuration* to enable the plugin.\n")
                .append("\n----\n")
                .append("plugin.").append(pluginUtils.getPluginId()).append(".enabled = true\n")
                .append("plugin.").append(pluginUtils.getPluginId()).append(".version = ").append(projectVersion)
                .append("\n----\n");

        if (StringUtils.isNotEmpty(pluginUtils.getFilterClass())){
            // Add note for append it to end of filter
            content.append("\nAdd `").append(pluginUtils.getFilterClass()).append("` to the filter chain (`filter.classes`):").append("\n")
                    .append("\nNOTE: The `").append(pluginUtils.getFilterClass()).append("` filter should be placed at an appropriate position in the filter chain. In most circumstances this should be located towards the end of the filter chain.\n")
                    .append("\n----\n")
                    .append("filter.classes=<OTHER-FILTERS>:").append(pluginUtils.getFilterClass()).append(":<OTHER-FILTERS> //<1>")
                    .append("\n----\n")
                    .append("<1> `<OTHER-FILTERS>` is a colon-delimited list of zero or more filters in the existing filter chain. \n");
        }

        if (StringUtils.isNotEmpty(pluginUtils.getJsoupFilterClass())){
            content.append("\nAdd `").append(pluginUtils.getJsoupFilterClass()).append("` to the jsoup filter chain (`filter.jsoup.classes`):").append("\n\n")
                    .append("[NOTE]\n")
                    .append("====\n")
                    .append("* The `").append(pluginUtils.getJsoupFilterClass()).append("` filter should be placed at an appropriate position in the Jsoup filter chain. In most circumstances, this should be located toward the end of the Jsoup filter chain.\n")
                    .append("* Jsoup filtering must be also enabled for this plugin to function. Check to see if there is a `filter.classes` set in the data source configuration. If it is set, the filter classes must include `JSoupProcessingFilterProvider` in the list of filters. If `filter.classes` is not set, then the default filter chain is applied and JSoup filtering is enabled.\n")
                    .append("====\n")
                    .append("\n----\n")
                    .append("filter.jsoup.classes=<OTHER-JSOUP-FILTERS>,").append(pluginUtils.getJsoupFilterClass()).append(",<OTHER-JSOUP-FILTERS> //<1>")
                    .append("\n----\n")
                    .append("<1> `<OTHER-JSOUP-FILTERS>` is a comma-delimited list of zero or more filters in the existing Jsoup filter chain. \n");
        }
        content.append("\nNOTE: The plugin will take effect after the configuration is published, and a full update of the data source has completed.\n");
    }

    private void addResultsPageConfigurations(){
        content.append("\n=== Enable the plugin on results page").append("\n");
        content.append("\nEnable the *").append(pluginUtils.getPluginName()).append("* plugin on your *")
                .append(PluginTarget.RESULTS_PAGE.getTarget().toLowerCase()).append("* from the *plugins* screen in the search dashboard or add the following *")
                .append(PluginTarget.RESULTS_PAGE.getTarget().toLowerCase())
                .append(" configuration* to enable the plugin.\n")
                .append("\n----\n")
                .append("plugin.").append(pluginUtils.getPluginId()).append(".enabled = true\n")
                .append("plugin.").append(pluginUtils.getPluginId()).append(".version = ").append(projectVersion)
                .append("\n----\n");

        content.append("\nNOTE: The plugin will take effect as soon as it is enabled and the configuration is published.\n");
    }
    private void writePluginConfigKeysToResourceAdoc() {
        List<PluginConfigKeyDetails> configKeys = pluginUtils.getConfigKeys();
        if (!configKeys.isEmpty()) {
            content.append("\n=== Plugin configuration settings\n");
            content.append("\nThe following options can be set in the ")
                    .append(pluginTargets)
                    .append(" configuration to configure the plugin:\n\n");

            for (PluginConfigKeyDetails configKey : configKeys) {
                if (configKey instanceof PluginConfigKey) {
                    PluginConfigKey<?> key = (PluginConfigKey<?>) configKey;
                    PluginConfigKeyType keyType = key.getType();
                    String keyTypeText = (keyType.equals(PluginConfigKeyType.Format.ARRAY))
                            ? keyType.getSubtype().getType()
                            : keyType.getType().getType();

                    content.append("* `")
                            .append(key.getKey())
                            .append("`: (")
                            .append(keyTypeText)
                            .append(") ")
                            .append(key.getDescription());
                } else if (configKey instanceof PluginConfigKeyEncrypted) {
                    PluginConfigKeyEncrypted encryptedKey = (PluginConfigKeyEncrypted) configKey;
                    content.append("* `")
                            .append(encryptedKey.getKey())
                            .append("`: ")
                            .append(encryptedKey.getDescription());
                }

                Object defaultValue = configKey.getDefaultValue();
                if (defaultValue instanceof List){
                    if (((List<?>) defaultValue).size() == 0){
                        defaultValue = "an empty list";
                    }
                }
                if (defaultValue != null) {
                    content.append("\n+\n");
                    content.append("Default value is `")
                            .append(defaultValue).append("`");
                }

                PluginConfigKeyAllowedValue allowedValue = configKey.getAllowedValue();
                if (allowedValue != null){
                    content.append("\n+\n");
                    if (allowedValue.getValues() != null && allowedValue.getValues().size() > 0) {
                        content.append("Allowed values are: `");
                        content.append(allowedValue.getValues().stream().map(Object::toString)
                                        .collect(Collectors.joining(",")));
                        content.append("`");

                    }
                    if (allowedValue.getRegex() != null){
                        content.append("Allowed values should adhere to regular expression: `++")
                                .append(allowedValue.getRegex().toString());
                        content.append("++`");
                    }
                }
                content.append("\n");
            }
        }
    }

    private void addConfigFileDetails(){
        if (pluginUtils.getConfigFiles() != null && pluginUtils.getConfigFiles().size() > 0){
            content.append("\n=== Plugin configuration files\n");
            for (PluginConfigFile file: pluginUtils.getConfigFiles()) {
                File configFileDetailsSource = new File(projectResourcePath + CONFIGFILE + file.getName() + ".adoc");
                content.append("\n==== Configuration file: ").append(file.getName()).append("\n");
                content.append("\n*Description:* ").append(file.getDescription()).append("\n");
                content.append("\n*Configuration file format:* ").append(file.getFormat()).append("\n\n");
                if (configFileDetailsSource.exists()){
                    content.append(readAsciiFile(configFileDetailsSource.getPath())).append("\n\n");
                }else {
                    content.append("WARNING: Details for plugin configuration file `" + file.getName()).append("` are not added. Please create the documentation in `/src/test/resources/ascii/sections/" + CONFIGFILE + file.getName() + ".adoc`\n\n");
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
        content.append("\n").append("== Examples\n");
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
