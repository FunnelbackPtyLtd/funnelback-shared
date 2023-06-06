package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.*;
import com.funnelback.plugin.docs.model.Audience;
import com.funnelback.plugin.docs.model.MarketplaceSubtype;
import com.funnelback.plugin.docs.model.ProductSubtopicCategory;
import com.funnelback.plugin.docs.model.ProductTopic;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AsciiDocGenerator {
    final static String FILE_NAME = "index.adoc";
    final static String DOCUMENTATION = "Documentation";
    final static String PLUGINS = "Plugins";
    final static String KEYWORDS = "keywords.adoc";
    final static String ADDITIONALCONFIGSETTINGS = "additionalconfigsettings.adoc";
    final static String CHANGELOG = "changelog.adoc";
    final static String EXAMPLE = "example.adoc";
    private final PluginUtilsBase pluginUtils;

    private final String resourcesPath;
    private  String projectResourcePath;
    private String packageName;
    private String pluginTargets;
    private StringBuilder content = new StringBuilder();
    AsciiDocGenerator(PluginUtilsBase pluginUtils, String resourcesPath, String packageName, String projectResourcePath) {
        this.pluginUtils = pluginUtils;
        this.resourcesPath = resourcesPath;
        this.packageName = packageName;
        this.projectResourcePath = projectResourcePath + "/ascii/sections/";
        this.pluginTargets = pluginUtils.getPluginTarget().stream()
                .map(PluginTarget::getTarget)
                .collect(Collectors.joining("|"));
    }

    public String generateASCIIDocument()  {
        try {
            content.append("= Plugin: ").append(pluginUtils.getPluginName()).append(" Properties\n\n");
            content.append("\n----\n");
            content.append("\ndescription: ").append(pluginUtils.getPluginDescription());
            content.append("\nkeywords: ");
            String result = readAsciiFile(projectResourcePath + KEYWORDS);
            int lastIndex = result.lastIndexOf("\n");
            content.append(result.substring(0, lastIndex));
            content.append("\ncontent-type: ").append(StringUtils.join(DOCUMENTATION, "|", PLUGINS));
            content.append("\nhc-audience: ").append(pluginUtils.getAudience().stream()
                                                        .map(Audience::getType)
                                                        .collect(Collectors.joining("|")));
            content.append("\nmarketplace-type: ").append("Plugin");
            content.append("\nmarketplace-subtype: ").append(pluginUtils.getMarketplaceSubtype().stream()
                                                                .map(MarketplaceSubtype::getType)
                                                                .collect(Collectors.joining("|")));
            content.append("\nmarketplace-version: ").append("1.0.0"); //Need to find out how to write version here
            content.append("\nplugin-scope: ").append(pluginTargets);
            content.append("\nplugin-package: ").append(packageName); // Try to fetch package
            content.append("\nplugin-id: ").append(pluginUtils.getPluginId());
            content.append("\nplugin-interface: ").append(getPluginInterfaces()); // Try to get interface
            content.append("\nplugin-topic: ").append(pluginUtils.getProductTopic().stream()
                                                        .map(ProductTopic::getTopic)
                                                        .collect(Collectors.joining("|")));
            content.append("\nplugin-subtopic: ").append(pluginUtils.getProductSubtopic().stream()
                                                            .map(ProductSubtopicCategory::getTopic)
                                                            .collect(Collectors.joining("|")));
            content.append("\n----\n");

            writeToFile( appendRemainingContent(content), resourcesPath, FILE_NAME);
            return content.toString();
        }catch (Exception e){
                throw new RuntimeException(e);
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

    private String appendRemainingContent(StringBuilder content){
        content.append("\n\n").append("== Purpose ").append("\n\n");
        content.append(pluginUtils.getPluginDescription()).append("\n\n");

        content.append("== Usage")
                .append("\n\n")
                .append("=== Enable the plugin").append("\n");
        content.append("\nEnable the *").append(pluginUtils.getPluginName()).append("* plugin on your *")
                .append(pluginTargets).append("* from the *plugins* screen in the search dashboard or add the following ")
                .append(pluginTargets)
                .append(" configuration to enable the plugin.\n\n")
                .append("\n----\n")
                .append("plugin.").append(pluginUtils.getPluginId()).append(".enabled=true\n")
                .append("plugin.").append(pluginUtils.getPluginId()).append(".version=1.0.0")
                .append("\n----\n");

        if (StringUtils.isNotEmpty(pluginUtils.getFilterClass())){
            content.append("\nAdd `").append(pluginUtils.getFilterClass()).append("` to the filter chain:").append("\n")
                    .append("\n----\n")
                    .append("filter.classes=<OTHER-FILTERS>:").append(pluginUtils.getFilterClass()).append(":<OTHER-FILTERS>")
                    .append("\n----\n");
        }
        if (StringUtils.isNotEmpty(pluginUtils.getJsoupFilterClass())){
            content.append("\nEnsure that Jsoup filtering is enabled: `filter.classes` is either not set in the configuration, or includes the value `JSoupProcessingFilterProvider`, then add the `")
                    .append(pluginUtils.getJsoupFilterClass()).append("` filter to the jsoup filter chain.")
                    .append("\n\nNOTE: The ").append(pluginUtils.getJsoupFilterClass()).append(" filter should be placed at an appropriate position in the filter chain. In most circumstances this should be located towards the end of the filter chain.")
                    .append("\n")
                    .append("\n----\n")
                    .append("filter.jsoup.classes=<OTHER-JSOUP-FILTERS>,").append(pluginUtils.getJsoupFilterClass()).append("<OTHER-JSOUP-FILTERS>")
                    .append("\n----\n");
        }
        if (pluginUtils.getPluginTarget().contains(PluginTarget.DATA_SOURCE)){
            content.append("\nNOTE: The plugin will take effect after a full update of the data source.\n");
        }else if(pluginUtils.getPluginTarget().contains(PluginTarget.RESULTS_PAGE)){
            content.append("\nNOTE: The plugin will take effect as soon as it is enabled.\n");
        }

        writePluginConfigToResourceAdoc();

        appendFileContents();
        String text = "plugins";
        String url = "https://docs.squiz.net/funnelback/docs/latest/build/plugins/index.html";
        String hyperlink = String.format("link:%s[%s]", url, text);
        content.append("\n\n== See also:\n\n").append(hyperlink);
        return content.toString();
    }

    private void writePluginConfigToResourceAdoc() {
        content.append("=== Plugin configuration settings\n\n");

        List<PluginConfigKeyDetails> configKeys = pluginUtils.getConfigKeys();
        if (!configKeys.isEmpty()) {
            content.append("\nThe following options can be set in the ")
                    .append(pluginTargets)
                    .append(" configuration to configure the plugin:\n")
                    .append("\n----\n");

            for (PluginConfigKeyDetails configKey : configKeys) {
                if (configKey instanceof PluginConfigKey) {
                    PluginConfigKey<?> key = (PluginConfigKey<?>) configKey;
                    PluginConfigKeyType keyType = key.getType();
                    String keyTypeText = (keyType.equals(PluginConfigKeyType.Format.ARRAY))
                            ? keyType.getSubtype().getType()
                            : keyType.getType().getType();

                    content.append("* `")
                            .append(key.getKey())
                            .append("` = (")
                            .append(keyTypeText)
                            .append(") ")
                            .append(key.getDescription());
                } else if (configKey instanceof PluginConfigKeyEncrypted) {
                    PluginConfigKeyEncrypted encryptedKey = (PluginConfigKeyEncrypted) configKey;
                    content.append("* `")
                            .append(encryptedKey.getKey())
                            .append("` = ")
                            .append(encryptedKey.getDescription());
                }

                Object defaultValue = configKey.getDefaultValue();
                if (defaultValue != null) {
                    content.append(". Default value is ")
                            .append(defaultValue)
                            .append("\n");
                }
            }
            content.append("\n----\n");
        }
    }


    // Readme file will talk about how to use pluginutils and how to  use ascii doc
    private void writeToFile( String content, String resourcesPath, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resourcesPath + "/" + filename))) {
            writer.write(content);
            System.out.println("Plugin Documentation generated successfully!");
        } catch (IOException e) {
            System.err.println("Error writing plugin documentation to file: " + e.getMessage());
        }
    }

    private void appendFileContents(){
        content.append("\n").append(readAsciiFile(projectResourcePath + ADDITIONALCONFIGSETTINGS));
        content.append("\n").append(readAsciiFile(projectResourcePath + EXAMPLE));
        content.append("\n").append(readAsciiFile(projectResourcePath + CHANGELOG));
    }
    private String readAsciiFile(String filePath){
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
                // Process each line of the ASCII file
                result.append(line).append("\n");
            }
            reader.close();
            return result.toString();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
