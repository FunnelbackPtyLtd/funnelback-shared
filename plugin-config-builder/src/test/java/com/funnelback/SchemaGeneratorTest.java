package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.PluginConfigFile;
import com.funnelback.plugin.details.model.PluginConfigKey;
import com.funnelback.plugin.details.model.PluginConfigKeyAllowedValue;
import com.funnelback.plugin.details.model.PluginConfigKeyEncrypted;
import com.funnelback.plugin.details.model.PluginConfigKeyType;
import com.funnelback.plugin.details.model.PluginTarget;
import com.funnelback.plugin.docs.model.ProductSubtopic;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class SchemaGeneratorTest {
    @Rule public TemporaryFolder tmpFolder = new TemporaryFolder();

    private final PluginUtilsBase pluginUtils;
    private final String expectedSchema;
    private final String desc;

    @Parameterized.Parameters(name = "{index}: {2}")
    public static List<Object> data() {
        return Arrays.asList(new Object[][] {{
            PluginUtilsStub.builder()
                .pluginId("test-id")
                .pluginName("test")
                .pluginDescription("test desc")
                .configKeys(List.of()).build(),
            "{\"configFiles\":null,\"pluginName\":\"test\",\"pluginId\":\"test-id\",\"pluginDescription\":\"test desc\",\"configKeys\":[],\"metadataTags\":null,\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
            "Basic"
        }, {
            PluginUtilsStub.builder()
                .configKeys(List.of(
                    PluginConfigKey.<Integer>builder()
                        .pluginId("test")
                        .id("int.*")
                        .type(PluginConfigKeyType.builder().type(PluginConfigKeyType.Format.INTEGER).build())
                        .defaultValue(2)
                        .allowedValue(new PluginConfigKeyAllowedValue <>(List.of(1, 2, 3)))
                        .label("key1")
                        .description("desc1").build(),
                    PluginConfigKeyEncrypted.builder()
                        .pluginId("test")
                        .id("pass")
                        .label("key2")
                        .description("desc2")
                        .required(true).build())).build(),
            "{\"configFiles\":null,\"pluginName\":null,\"pluginId\":null,\"pluginDescription\":null,\"configKeys\":[{\"defaultValue\":2,\"showIfKeyHasValue\":null,\"description\":\"desc1\",\"id\":\"int.*\",\"allowedValue\":{\"regex\":null,\"values\":[1,2,3],\"type\":\"FIXED_LIST\"},\"label\":\"key1\",\"type\":{\"subtype\":null,\"type\":\"INTEGER\"},\"key\":\"plugin.test.config.int.*\",\"required\":false},{\"defaultValue\":null,\"showIfKeyHasValue\":null,\"description\":\"desc2\",\"id\":\"pass\",\"allowedValue\":null,\"label\":\"key2\",\"type\":{\"subtype\":null,\"type\":\"PASSWORD\"},\"key\":\"plugin.test.encrypted.pass\",\"required\":true}],\"metadataTags\":null,\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
            "With config keys"
        }, {
            PluginUtilsStub.builder()
                .configFiles(List.of(PluginConfigFile.builder()
                    .name("rules.cfg")
                    .label("rules")
                    .description("desc")
                    .format("json")
                    .required(true).build())).build(),
            "{\"configFiles\":[{\"format\":\"json\",\"name\":\"rules.cfg\",\"description\":\"desc\",\"label\":\"rules\",\"required\":true}],\"pluginName\":null,\"pluginId\":null,\"pluginDescription\":null,\"configKeys\":null,\"metadataTags\":null,\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
            "With config file"
        }, {
            PluginUtilsStub.builder()
                .pluginTarget(List.of(PluginTarget.DATA_SOURCE, PluginTarget.RESULTS_PAGE)).build(),
            "{\"configFiles\":null,\"pluginName\":null,\"pluginId\":null,\"pluginDescription\":null,\"configKeys\":null,\"metadataTags\":null,\"pluginTarget\":[\"Data source\",\"Results page\"],\"filterClass\":null,\"jsoupFilterClass\":null}",
            "With plugin targets (scope)"
        }, {
            PluginUtilsStub.builder()
                .productSubtopic(List.of(ProductSubtopic.Indexing.DOCUMENT_FILTERS, ProductSubtopic.ResultsPage.BEST_BETS)).build(),
            "{\"configFiles\":null,\"pluginName\":null,\"pluginId\":null,\"pluginDescription\":null,\"configKeys\":null,\"metadataTags\":[\"Document filters\",\"Best bets\"],\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
            "With metadata tags"
        }});
    }

    @Test
    public void test() throws IOException, JSONException {
        File resourcesFolder = tmpFolder.newFolder();
        new SchemaGenerator(pluginUtils, resourcesFolder.getCanonicalPath()).generate();
        String actual = Files.readString(new File(resourcesFolder, SchemaGenerator.FILE_NAME).toPath());
        JSONAssert.assertEquals(expectedSchema, actual, true);
    }

}