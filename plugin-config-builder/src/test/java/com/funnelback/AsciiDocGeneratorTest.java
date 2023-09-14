package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.*;
import com.funnelback.plugin.docs.model.Audience;
import com.funnelback.plugin.docs.model.MarketplaceSubtype;
import com.funnelback.plugin.docs.model.ProductSubtopic;
import com.funnelback.plugin.docs.model.ProductTopic;
import lombok.RequiredArgsConstructor;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class AsciiDocGeneratorTest {

    @Rule public TemporaryFolder tmpFolder = new TemporaryFolder();

    private final PluginUtilsBase pluginUtils;
    private final String expectedFile;

    private final String desc;

    @Parameterized.Parameters(name = "{index}: {2}")
    public static List<Object> data() {
        return Arrays.asList(new Object[][]{{
                PluginUtilsStub.builder()
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
                                        .longDescription("== H2 title\n" +
                                                "\n" +
                                                "This is a *long description* which includes _asciidoc formatting_.\n" +
                                                "\n" +
                                                "[source,json]\n" +
                                                "----\n" +
                                                "plugin.example=blah\n" +
                                                "----\n" +
                                                "\n" +
                                                "NOTE: This is only an example!").build(),
                                PluginConfigKey.<Integer>builder()
                                        .pluginId("test")
                                        .id("int.*")
                                        .type(PluginConfigKeyType.builder().type(PluginConfigKeyType.Format.STRING).build())
                                        .defaultValue(2)
                                        .allowedValue(new PluginConfigKeyAllowedValue<>(Pattern.compile("^[a-zA-Z0-9]+$")))
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
                                        .build())).build(),
                "{\"configFiles\":null,\"pluginName\":\"test\",\"pluginId\":\"test-id\",\"pluginDescription\":\"test desc\",\"configKeys\":[],\"metadataTags\":null,\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
                "Basic"
            }
        });
    }

    @Test
    public void test() throws IOException {
        File resourcesFolder = tmpFolder.newFolder();
        File projectResourcesFolder = new File("src/test/resources");
        new AsciiDocGenerator(pluginUtils, resourcesFolder.getCanonicalPath(),"example", "1.0.0", projectResourcesFolder.getCanonicalPath()).generateASCIIDocument();
        String actual = Files.readString(new File(resourcesFolder, AsciiDocGenerator.FILE_NAME).toPath());
        Assert.assertEquals(getExpectedFile(), actual);
    }

    private String getExpectedFile(){
        return "= Plugin: test\n" +
                ":page-description: test desc\n" +
                ":page-keywords: keyword1, keyword2, keyword3\n" +
                ":page-content-type: Documentation|Plugins\n" +
                ":page-hc-audience: Site builder|Administrator|Content editor\n" +
                ":page-marketplace-type: Plugin\n" +
                ":page-marketplace-subtype: Custom gatherer|Search lifecycle\n" +
                ":page-marketplace-version: 1.0.0\n" +
                ":page-plugin-scope: Data source|Results page\n" +
                ":page-plugin-package: example\n" +
                ":page-plugin-id: test-id\n" +
                ":page-plugin-interface: gatherer|searchLifeCycle\n" +
                ":page-product-topic: Analytics and reporting\n" +
                ":page-product-subtopic: Performance|Custom|Index manipulation\n" +
                "\n" +
                "== Purpose \n" +
                "\n" +
                "test desc\n" +
                "\n" +
                "// ==========\n" +
                "// This file contains a detailed description of the plugin.\n" +
                "//\n" +
                "// See: https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#detailed-desription for more details and examples.\n" +
                "// ==========\n" +
                "\n" +
                "This is a detailed description for test plugin.\n" +
                "\n" +
                "This is a super cool plugin which can do wonders !!!\n" +
                "\n" +
                "Happy plugining !!\n" +
                "\n" +
                "\n" +
                "== Usage\n" +
                "\n" +
                "=== Enable the plugin\n" +
                "\n" +
                ". Select menu:Plugins[] from the side navigation pane and click on the *test* tile.\n" +
                ". From the *Location* section, decide if you wish to enable this plugin on a *data source* or a *results page* and select the corresponding radio button.\n" +
                ". Select the data source or results page to which you would like to enable this plugin from the drop-down menu.\n" +
                "\n" +
                "NOTE: If enabled on a data source, the plugin will take effect as soon as the setup steps are completed, and an advanced > full update of the data source has completed. If enabled on a results page the plugin will take effect as soon as the setup steps are completed.\n" +
                "\n" +
                "=== Configuration settings\n" +
                "\n" +
                "The *configuration settings* section is where you do most of the configuration for your plugin. The settings enable you to control how the plugin behaves.\n" +
                "\n" +
                "NOTE: The configuration key names below are only used if you are configuring this plugin manually. The configuration keys are set in the data source or results page configuration to configure the plugin. When setting the keys manually you need to type in (or copy and paste) the key name and value.\n" +
                "\n" +
                "==== key1\n" +
                "\n" +
                "[%autowidth.spread]\n" +
                "|===\n" +
                "|Configuration key| `plugin.test.config.int.*`\n" +
                "|Data type|integer\n" +
                "|Default value|`+2+`\n" +
                "|Allowed values|1,2,3\n" +
                "|Required|This setting is required\n" +
                "|===\n" +
                "\n" +
                "desc1\n" +
                "\n" +
                "== H2 title\n" +
                "\n" +
                "This is a *long description* which includes _asciidoc formatting_.\n" +
                "\n" +
                "[source,json]\n" +
                "----\n" +
                "plugin.example=blah\n" +
                "----\n" +
                "\n" +
                "NOTE: This is only an example!\n" +
                "\n" +
                "==== key2\n" +
                "\n" +
                "[%autowidth.spread]\n" +
                "|===\n" +
                "|Configuration key| `plugin.test.config.int.*`\n" +
                "|Data type|string\n" +
                "|Default value|`+2+`\n" +
                "|Value format|Allowed values must match the regular expression:\n" +
                "\n" +
                "`++^[a-zA-Z0-9]+$++`\n" +
                "|Required|This setting is optional\n" +
                "|===\n" +
                "\n" +
                "desc2\n" +
                "\n" +
                "==== key3\n" +
                "\n" +
                "[%autowidth.spread]\n" +
                "|===\n" +
                "|Configuration key| `plugin.test.encrypted.pass`\n" +
                "|Data type|Encrypted string\n" +
                "|Required|This setting is required\n" +
                "|===\n" +
                "\n" +
                "desc3\n" +
                "\n" +
                "This password must be 15 characters long with _special_ characters.\n" +
                "\n" +
                "==== List key\n" +
                "\n" +
                "[%autowidth.spread]\n" +
                "|===\n" +
                "|Configuration key| `plugin.test.config.list`\n" +
                "|Data type|array\n" +
                "|Default value|`+an empty list+`\n" +
                "|Required|This setting is optional\n" +
                "|===\n" +
                "\n" +
                "Define a list of strings\n" +
                "\n" +
                "=== Additional configuration settings\n" +
                "\n" +
                "// ==========\n" +
                "// Additional information about plugin configuration settings to be listed here.\n" +
                "//\n" +
                "// See: https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#additional-config-settings more details and examples.\n" +
                "// ==========\n" +
                "\n" +
                "=== Filter chain configuration\n" +
                "\n" +
                "This plugin uses filters which are used to apply transformations to the gathered content.\n" +
                "\n" +
                "The filters run in sequence and need be set in an order that makes sense. The plugin supplied filter(s) (as indicated in the listing) should be re-ordered to an appropriate point in the sequence.\n" +
                "\n" +
                "WARNING: Changes to the filter order affects the way the data source processes gathered documents. See: xref:build/data-sources/document-filtering/index.adoc[document filters documentation].\n" +
                "\n" +
                "==== Filter classes\n" +
                "\n" +
                "This plugin supplies a filter that runs in the main document filter chain: `+test-plugin-filter1:filter2+`\n" +
                "\n" +
                "Drag the *+test-plugin-filter1:filter2+* plugin filter to where you wish it to run in the filter chain sequence.\n" +
                "\n" +
                "==== Jsoup filter classes\n" +
                "\n" +
                "This plugin supplies a filter that needs to run in the HTML document (Jsoup) filter chain:`+test-plugin-jsoup-filter1+`\n" +
                "\n" +
                "Drag the *+test-plugin-jsoup-filter1+* plugin filter to where you wish it to run in the filter chain sequence.\n" +
                "\n" +
                "=== Configuration files\n" +
                "\n" +
                "This plugin also uses the following configuration files to provide additional configuration.\n" +
                "\n" +
                "==== config-rules.cfg\n" +
                "\n" +
                "[%autowidth.spread]\n" +
                "|===\n" +
                "|Description|List of rules to gather data\n" +
                "|Configuration file format|json\n" +
                "|===\n" +
                "\n" +
                "Example: HTML source input file (http://example.com/itemdirectory/search.html)\n" +
                "[source, xml]\n" +
                "----\n" +
                "<html>\n" +
                "<body>\n" +
                "<div>\n" +
                "<ul class=\"item-list\">\n" +
                "<li class=\"item\"> Item 1 </li>\n" +
                "<li class=\"item\"> Item 2 </li>\n" +
                "<li class=\"item\"> Item 3 </li>\n" +
                "<li class=\"item\"> Item 4 </li>\n" +
                "<li class=\"item\"> Item 5 </li>\n" +
                "<li class=\"item\"> Item 6 </li>\n" +
                "</ul>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n" +
                "\n" +
                "----\n" +
                "\n" +
                "\n" +
                "==== test.cfg\n" +
                "\n" +
                "[%autowidth.spread]\n" +
                "|===\n" +
                "|Description|test\n" +
                "|Configuration file format|json\n" +
                "|===\n" +
                "\n" +
                "WARNING: Details for plugin configuration file `test.cfg` are not added. Please create the documentation in `/src/test/resources/ascii/sections/configfile_test.cfg.adoc`\n" +
                "\n" +
                "\n" +
                "== Examples\n" +
                "\n" +
                "WARNING: It is recommended to include at least one example about usage of the plugin.\n" +
                "\n" +
                "== Change log\n" +
                "// ==========\n" +
                "// Provide log what has changed in each version of the plugin.\n" +
                "//\n" +
                "// See: https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#change-log more details and examples.\n" +
                "// ==========\n" +
                "\n" +
                "\n" +
                "== See also\n" +
                "\n" +
                "* xref:build/plugins/index.adoc[Plugins]\n" +
                "// ==========\n" +
                "// Add additional references as bullet points in this file.\n" +
                "// https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#see-also\n" +
                "// ==========\n" +
                "\n";
    }
}
