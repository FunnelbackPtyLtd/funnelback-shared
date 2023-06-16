package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.*;
import com.funnelback.plugin.docs.model.Audience;
import com.funnelback.plugin.docs.model.MarketplaceSubtype;
import com.funnelback.plugin.docs.model.ProductSubtopic;
import com.funnelback.plugin.docs.model.ProductTopic;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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
                                        .description("desc1").build(),
                                PluginConfigKey.<Integer>builder()
                                        .pluginId("test")
                                        .id("int.*")
                                        .type(PluginConfigKeyType.builder().type(PluginConfigKeyType.Format.STRING).build())
                                        .defaultValue(2)
                                        .allowedValue(new PluginConfigKeyAllowedValue<>(Pattern.compile("^[a-zA-Z0-9]+$")))
                                        .label("key2")
                                        .description("desc2").build(),
                                PluginConfigKeyEncrypted.builder()
                                        .pluginId("test")
                                        .id("pass")
                                        .label("key3")
                                        .description("desc3")
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
    public void test() throws IOException, JSONException {
        File resourcesFolder = tmpFolder.newFolder();
        File projectResourcesFolder = new File("src/test/resources");
        new AsciiDocGenerator(pluginUtils, resourcesFolder.getCanonicalPath(),"example", "1.0.0", projectResourcesFolder.getCanonicalPath()).generateASCIIDocument();
        String actual = Files.readString(new File(resourcesFolder, AsciiDocGenerator.FILE_NAME).toPath());
        Assert.assertEquals(getExpectedFile(), actual);
    }

    private String getExpectedFile(){
        return "= Plugin: test\n" +
                "\n" +
                ":page-description: test desc\n" +
                ":page-keywords: keyword1, keyword2, keyword3\n" +
                ":page-content-type: Documentation|Plugins\n" +
                ":page-hc-audience: Site builder|Administrator|Content editor\n" +
                ":page-marketplace-type: Plugin\n" +
                ":page-marketplace-subtype: Custom gatherer|Search lifecycle\n" +
                ":page-marketplace-version: 1.0.0\n" +
                ":page-plugin-scope: data source or results page\n" +
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
                "=== Enable the plugin on data source\n" +
                "\n" +
                "Enable the *test* plugin on your *data source* from the *plugins* screen in the search dashboard or add the following *data source configuration* to enable the plugin.\n" +
                "\n" +
                "----\n" +
                "plugin.test-id.enabled = true\n" +
                "plugin.test-id.version = 1.0.0\n" +
                "----\n" +
                "\n" +
                "Add `test-plugin-filter1:filter2` to the filter chain (`filter.classes`):\n" +
                "\n" +
                "NOTE: The `test-plugin-filter1:filter2` filter should be placed at an appropriate position in the filter chain. In most circumstances this should be located towards the end of the filter chain.\n" +
                "\n" +
                "----\n" +
                "filter.classes=<OTHER-FILTERS>:test-plugin-filter1:filter2:<OTHER-FILTERS> //<1>\n" +
                "----\n" +
                "<1> `<OTHER-FILTERS>` is a colon-delimited list of zero or more filters in the existing filter chain. \n" +
                "\n" +
                "Add `test-plugin-jsoup-filter1` to the jsoup filter chain (`filter.jsoup.classes`):\n" +
                "\n" +
                "[NOTE]\n" +
                "====\n" +
                "* The test-plugin-jsoup-filter1 filter should be placed at an appropriate position in the Jsoup filter chain. In most circumstances, this should be located toward the end of the Jsoup filter chain.\n" +
                "* Jsoup filtering must be also enabled for this plugin to function. Check to see if there is a `filter.classes` set in the data source configuration. If it is set, the filter classes must include `JSoupProcessingFilterProvider` in the list of filters. If `filter.classes` is not set, then the default filter chain is applied and JSoup filtering is enabled.\n" +
                "====\n" +
                "\n" +
                "----\n" +
                "filter.jsoup.classes=<OTHER-JSOUP-FILTERS>,test-plugin-jsoup-filter1,<OTHER-JSOUP-FILTERS> //<1>\n" +
                "----\n" +
                "<1> `<OTHER-JSOUP-FILTERS>` is a comma-delimited list of zero or more filters in the existing Jsoup filter chain. \n" +
                "\n" +
                "NOTE: The plugin will take effect after the configuration is published, and a full update of the data source has completed.\n" +
                "\n" +
                "=== Enable the plugin on results page\n" +
                "\n" +
                "Enable the *test* plugin on your *results page* from the *plugins* screen in the search dashboard or add the following *results page configuration* to enable the plugin.\n" +
                "\n" +
                "----\n" +
                "plugin.test-id.enabled = true\n" +
                "plugin.test-id.version = 1.0.0\n" +
                "----\n" +
                "\n" +
                "NOTE: The plugin will take effect as soon as it is enabled and the configuration is published.\n" +
                "\n" +
                "=== Plugin configuration settings\n" +
                "\n" +
                "The following options can be set in the data source or results page configuration to configure the plugin:\n" +
                "\n" +
                "* `plugin.test.config.int.*`: (integer) desc1\n" +
                "+\n" +
                "Default value is `2`\n" +
                "+\n" +
                "Allowed values are: `1,2,3`\n" +
                "* `plugin.test.config.int.*`: (string) desc2\n" +
                "+\n" +
                "Default value is `2`\n" +
                "+\n" +
                "Allowed values should adhere to regular expression: `++^[a-zA-Z0-9]+$++`\n" +
                "* `plugin.test.encrypted.pass`: desc3\n" +
                "* `plugin.test.config.list`: (array) Define a list of strings\n" +
                "+\n" +
                "Default value is `an empty list`\n" +
                "\n" +
                "=== Additional configuration settings\n" +
                "\n" +
                "// ==========\n" +
                "// Additional information about plugin configuration settings to be listed here.\n" +
                "//\n" +
                "// See: https://docs.squiz.net/funnelback/docs/latest/develop/plugins/documentation/index.html#additional-config-settings more details and examples.\n" +
                "// ==========\n" +
                "\n" +
                "=== Plugin configuration files\n" +
                "\n" +
                "==== Configuration file: config-rules.cfg\n" +
                "\n" +
                "*Description:* List of rules to gather data\n" +
                "\n" +
                "*Configuration file format:* json\n" +
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
                "\n" +
                "==== Configuration file: test.cfg\n" +
                "\n" +
                "*Description:* test\n" +
                "\n" +
                "*Configuration file format:* json\n" +
                "\n" +
                "WARNING: Details for plugin configuration file `test.cfg` are not added. Please create the documentation in `/src/test/resources/ascii/sections/configfile_test.cfg.adoc`\n" +
                "\n" +
                "\n" +
                "== Examples\n" +
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
