package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.PluginConfigFile;
import com.funnelback.plugin.details.model.PluginConfigKey;
import com.funnelback.plugin.details.model.PluginConfigKeyAllowedValue;
import com.funnelback.plugin.details.model.PluginConfigKeyEncrypted;
import com.funnelback.plugin.details.model.PluginConfigKeyType;
import com.funnelback.plugin.details.model.PluginTarget;
import com.funnelback.plugin.docs.model.Audience;
import com.funnelback.plugin.docs.model.MarketplaceSubtype;
import com.funnelback.plugin.docs.model.ProductSubtopic;
import com.funnelback.plugin.docs.model.ProductTopic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

public class AsciiDocGeneratorTest {
    @TempDir private File tmpFolder;

    private final PluginUtilsBase pluginUtils = PluginUtilsStub.builder()
        .pluginId("test-id")
        .pluginName("test")
        .pluginDescription("test desc")
        .pluginTarget(List.of(PluginTarget.DATA_SOURCE, PluginTarget.RESULTS_PAGE))
        .filterClass("test-plugin-filter1:filter2")
        .audience(List.of(Audience.SITE_BUILDER, Audience.ADMINISTRATOR, Audience.CONTENT_EDITOR))
        .marketplaceSubtype(List.of(MarketplaceSubtype.GATHERER, MarketplaceSubtype.SEARCH_LIFECYCLE))
        .productTopic(List.of(ProductTopic.ANALYTICS_REPORTING))
        .productSubtopic(List.of(ProductSubtopic.IntegrationDevelopment.PERFORMANCE, ProductSubtopic.DataSources.CUSTOM, ProductSubtopic.Indexing.INDEX_MANIPULATION))
        .jsoupFilterClass("test-plugin-jsoup-filter1")
        .configFiles(List.of(PluginConfigFile.builder()
                .name("config-rules.cfg")
                .format("json")
                .label("Config file")
                .description("List of rules to gather data")
                .build(),
            PluginConfigFile.builder()
                .name("test.cfg")
                .format("json")
                .label("test file")
                .description("test")
                .build()))
        .configKeys(List.of(
            PluginConfigKey.<Integer>builder()
                .pluginId("test")
                .id("int.*")
                .type(PluginConfigKeyType.builder().type(PluginConfigKeyType.Format.INTEGER).build())
                .defaultValue(2)
                .allowedValue(new PluginConfigKeyAllowedValue<>(List.of(1, 2, 3)))
                .label("key1")
                .required(true)
                .description("desc1")
                .longDescription("""
                        == H2 title
                        
                        This is a *long description* which includes _asciidoc formatting_.
                        
                        [source,json]
                        ----
                        plugin.example=blah
                        ----
                        
                        NOTE: This is only an example!""").build(),
            PluginConfigKey.<Integer>builder()
                .pluginId("test")
                .id("int.*")
                .type(PluginConfigKeyType.builder().type(PluginConfigKeyType.Format.STRING).build())
                .defaultValue(2)
                .allowedValue(new PluginConfigKeyAllowedValue<>(Pattern.compile("^[a-zA-Z0-9]+|NULL$")))
                .label("key2")
                .required(false)
                .description("desc2").build(),
            PluginConfigKeyEncrypted.builder()
                .pluginId("test")
                .id("pass")
                .label("key3")
                .description("desc3")
                .longDescription("This password must be 15 characters long with _special_ characters.")
                .required(true).build(),
            PluginConfigKey.<List<String>>builder()
                .pluginId("test")
                .id("list")
                .type(PluginConfigKeyType.builder().type(PluginConfigKeyType.Format.ARRAY).subtype(PluginConfigKeyType.Format.STRING).build())
                .defaultValue(List.of())
                .label("List key")
                .description("Define a list of strings")
                .build())).build();

    @Test
    public void test() throws IOException {
        File projectResourcesFolder = new File("src/test/resources");
        new AsciiDocGenerator(pluginUtils, tmpFolder.getCanonicalPath(),"example", "1.0.0", projectResourcesFolder.getCanonicalPath()).generateASCIIDocument();
        String actual = Files.readString(new File(tmpFolder, AsciiDocGenerator.FILE_NAME).toPath());
        Assertions.assertEquals(getExpectedFile(), actual);
    }

    private String getExpectedFile(){
        return """
                = Plugin: test
                :page-description: test desc
                :page-keywords: keyword1, keyword2, keyword3
                :page-content-type: Documentation|Plugins
                :page-hc-audience: Site builder|Administrator|Content editor
                :page-marketplace-type: Plugin
                :page-marketplace-subtype: Custom gatherer|Search lifecycle
                :page-marketplace-version: 1.0.0
                :page-plugin-scope: Data source|Results page
                :page-plugin-package: example
                :page-plugin-id: test-id
                :page-plugin-interface: gatherer|searchLifeCycle
                :page-product-topic: Analytics and reporting
                :page-product-subtopic: Performance|Custom|Index manipulation
                
                == Purpose\s
                
                test desc
                
                // ==========
                // This file contains a detailed description of the plugin.
                //
                // See: https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#detailed-desription for more details and examples.
                // ==========
                
                This is a detailed description for test plugin.
                
                This is a super cool plugin which can do wonders !!!
                
                Happy plugining !!
                
                
                == Usage
                
                === Enable the plugin
                
                . Select menu:Plugins[] from the side navigation pane and click on the *test* tile.
                . From the *Location* section, decide if you wish to enable this plugin on a *data source* or a *results page* and select the corresponding radio button.
                . Select the data source or results page to which you would like to enable this plugin from the drop-down menu.
                
                NOTE: If enabled on a data source, the plugin will take effect as soon as the setup steps are completed, and an advanced > full update of the data source has completed. If enabled on a results page the plugin will take effect as soon as the setup steps are completed.
                
                === Configuration settings
                
                The *configuration settings* section is where you do most of the configuration for your plugin. The settings enable you to control how the plugin behaves.
                
                NOTE: The configuration key names below are only used if you are configuring this plugin manually. The configuration keys are set in the data source or results page configuration to configure the plugin. When setting the keys manually you need to type in (or copy and paste) the key name and value.
                
                ==== key1
                
                [%autowidth.spread]
                |===
                |Configuration key| `+plugin.test.config.int.*+`
                |Data type|integer
                |Default value|`+2+`
                |Allowed values|1,2,3
                |Required|This setting is required
                |===
                
                desc1
                
                == H2 title
                
                This is a *long description* which includes _asciidoc formatting_.
                
                [source,json]
                ----
                plugin.example=blah
                ----
                
                NOTE: This is only an example!
                
                ==== key2
                
                [%autowidth.spread]
                |===
                |Configuration key| `+plugin.test.config.int.*+`
                |Data type|string
                |Default value|`+2+`
                |Value format|Allowed values must match the regular expression:
                
                `++^[a-zA-Z0-9]+\\|NULL$++`
                |Required|This setting is optional
                |===
                
                desc2
                
                ==== key3
                
                [%autowidth.spread]
                |===
                |Configuration key| `+plugin.test.encrypted.pass+`
                |Data type|Encrypted string
                |Required|This setting is required
                |===
                
                desc3
                
                This password must be 15 characters long with _special_ characters.
                
                ==== List key
                
                [%autowidth.spread]
                |===
                |Configuration key| `+plugin.test.config.list+`
                |Data type|array
                |Default value|`+an empty list+`
                |Required|This setting is optional
                |===
                
                Define a list of strings
                
                === Additional configuration settings
                
                // ==========
                // Additional information about plugin configuration settings to be listed here.
                //
                // See: https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#additional-config-settings more details and examples.
                // ==========
                
                === Filter chain configuration
                
                This plugin uses filters which are used to apply transformations to the gathered content.
                
                The filters run in sequence and need be set in an order that makes sense. The plugin supplied filter(s) (as indicated in the listing) should be re-ordered to an appropriate point in the sequence.
                
                WARNING: Changes to the filter order affects the way the data source processes gathered documents. See: xref:build/data-sources/document-filtering/index.adoc[document filters documentation].
                
                ==== Filter classes
                
                This plugin supplies a filter that runs in the main document filter chain: `+test-plugin-filter1:filter2+`
                
                Drag the *+test-plugin-filter1:filter2+* plugin filter to where you wish it to run in the filter chain sequence.
                
                ==== Jsoup filter classes
                
                This plugin supplies a filter that needs to run in the HTML document (Jsoup) filter chain:`+test-plugin-jsoup-filter1+`
                
                Drag the *+test-plugin-jsoup-filter1+* plugin filter to where you wish it to run in the filter chain sequence.
                
                === Configuration files
                
                This plugin also uses the following configuration files to provide additional configuration.
                
                ==== config-rules.cfg
                
                [%autowidth.spread]
                |===
                |Description|List of rules to gather data
                |Configuration file format|json
                |===
                
                Example: HTML source input file (http://example.com/itemdirectory/search.html)
                [source, xml]
                ----
                <html>
                <body>
                <div>
                <ul class="item-list">
                <li class="item"> Item 1 </li>
                <li class="item"> Item 2 </li>
                <li class="item"> Item 3 </li>
                <li class="item"> Item 4 </li>
                <li class="item"> Item 5 </li>
                <li class="item"> Item 6 </li>
                </ul>
                </div>
                </body>
                </html>
                
                ----
                
                
                ==== test.cfg
                
                [%autowidth.spread]
                |===
                |Description|test
                |Configuration file format|json
                |===
                
                WARNING: Details for plugin configuration file `test.cfg` are not added. Please create the documentation in `/src/test/resources/ascii/sections/configfile_test.cfg.adoc`
                
                
                == Examples
                
                WARNING: It is recommended to include at least one example about usage of the plugin.
                
                == Change log
                // ==========
                // Provide log what has changed in each version of the plugin.
                //
                // See: https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#change-log more details and examples.
                // ==========
                
                
                == See also
                
                * xref:build/plugins/index.adoc[Plugins]
                // ==========
                // Add additional references as bullet points in this file.
                // https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#see-also
                // ==========
                
                """;
    }
}
