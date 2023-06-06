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
import java.util.Arrays;
import java.util.List;

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
                        .configKeys(List.of(
                                PluginConfigKey.<Integer>builder()
                                        .pluginId("test")
                                        .id("int.*")
                                        .type(PluginConfigKeyType.builder().type(PluginConfigKeyType.Format.INTEGER).build())
                                        .defaultValue(2)
                                        .allowedValue(new PluginConfigKeyAllowedValue<>(List.of(1, 2, 3)))
                                        .label("key1")
                                        .description("desc1").build(),
                                PluginConfigKeyEncrypted.builder()
                                        .pluginId("test")
                                        .id("pass")
                                        .label("key2")
                                        .description("desc2")
                                        .required(true).build())).build(),
                "{\"configFiles\":null,\"pluginName\":\"test\",\"pluginId\":\"test-id\",\"pluginDescription\":\"test desc\",\"configKeys\":[],\"metadataTags\":null,\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
                "Basic"
        }
        });
    }

    @Test
    public void test() throws IOException, JSONException {
        File resourcesFolder = tmpFolder.newFolder();
        File projectResourcesFolder = new File("src/test/resources");
        new AsciiDocGenerator(pluginUtils, resourcesFolder.getCanonicalPath(),"example", projectResourcesFolder.getCanonicalPath()).generateASCIIDocument();
        String actual = Files.readString(new File(resourcesFolder, AsciiDocGenerator.FILE_NAME).toPath());
        String expected = "= Plugin: test Properties\n" +
                "\n" +
                "\n" +
                "----\n" +
                "\n" +
                "description: test desc\n" +
                "keywords: keyword1, keyword2, keyword3\n" +
                "content-type: Documentation|Plugins\n" +
                "hc-audience: Site builder|Administrator|Content editor\n" +
                "marketplace-type: Plugin\n" +
                "marketplace-subtype: Custom gatherer|Search lifecycle\n" +
                "marketplace-version: 1.0.0\n" +
                "plugin-scope: Data source|Results page\n" +
                "plugin-package: example\n" +
                "plugin-id: test-id\n" +
                "plugin-interface: gatherer|searchLifeCycle\n" +
                "plugin-topic: Analytics and reporting\n" +
                "plugin-subtopic: Performance|Custom|Index manipulation\n" +
                "----\n" +
                "\n" +
                "\n" +
                "== Purpose \n" +
                "\n" +
                "test desc\n" +
                "\n" +
                "== Usage\n" +
                "\n" +
                "=== Enable the plugin\n" +
                "\n" +
                "Enable the *test* plugin on your *Data source|Results page* from the *plugins* screen in the search dashboard or add the following Data source|Results page configuration to enable the plugin.\n" +
                "\n" +
                "\n" +
                "----\n" +
                "plugin.test-id.enabled=true\n" +
                "plugin.test-id.version=1.0.0\n" +
                "----\n" +
                "\n" +
                "Add `test-plugin-filter1:filter2` to the filter chain:\n" +
                "\n" +
                "----\n" +
                "filter.classes=<OTHER-FILTERS>:test-plugin-filter1:filter2:<OTHER-FILTERS>\n" +
                "----\n" +
                "\n" +
                "Ensure that Jsoup filtering is enabled: `filter.classes` is either not set in the configuration, or includes the value `JSoupProcessingFilterProvider`, then add the `test-plugin-jsoup-filter1` filter to the jsoup filter chain.\n" +
                "\n" +
                "NOTE: The test-plugin-jsoup-filter1 filter should be placed at an appropriate position in the filter chain. In most circumstances this should be located towards the end of the filter chain.\n" +
                "\n" +
                "----\n" +
                "filter.jsoup.classes=<OTHER-JSOUP-FILTERS>,test-plugin-jsoup-filter1<OTHER-JSOUP-FILTERS>\n" +
                "----\n" +
                "\n" +
                "NOTE: The plugin will take effect after a full update of the data source.\n" +
                "=== Plugin configuration settings\n" +
                "\n" +
                "\n" +
                "The following options can be set in the Data source|Results page configuration to configure the plugin:\n" +
                "\n" +
                "----\n" +
                "* `plugin.test.config.int.*` = (integer) desc1. Default value is 2\n" +
                "* `plugin.test.encrypted.pass` = desc2\n" +
                "----\n" +
                "\n" +
                "=== Additional configuration settings\n" +
                "\n" +
                "Additional information about plugin configuration settings is listed here:\n" +
                "\n" +
                "* `Option 1/ Plugin config key 1` : Additional information about usage or implementation of this option or key.\n" +
                "* `Option 2/ Plugin config key 2` : Additional information about usage or implementation of this option or key.\n" +
                "* `Option 3/ Plugin config key 3` : Additional information about usage or implementation of this option or key.\n" +
                "\n" +
                "== Example\n" +
                "\n" +
                "Provide examples of plugin configuration and usage.\n" +
                "\n" +
                "== Plugin upgrade instructions\n" +
                "\n" +
                "Provide log what has changed in each version of the plugin.\n" +
                "\n" +
                "Recommended structure (see https://keepachangelog.com/en/1.0.0/):\n" +
                "\n" +
                "Itemize your changes under the following headings:\n" +
                "\n" +
                "* Added ...\n" +
                "\n" +
                "* Changed ...\n" +
                "\n" +
                "* Deprecated ... (for soon-to-be-removed items)\n" +
                "\n" +
                "* Removed ... (for items that are now removed)\n" +
                "\n" +
                "* Fixed ...\n" +
                "\n" +
                "\n" +
                "== See also:\n" +
                "\n" +
                "link:https://docs.squiz.net/funnelback/docs/latest/build/plugins/index.html[plugins]";
        Assert.assertEquals(expected, actual);
    }
}
