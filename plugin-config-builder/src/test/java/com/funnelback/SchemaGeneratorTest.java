package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.PluginConfigFile;
import com.funnelback.plugin.details.model.PluginConfigKey;
import com.funnelback.plugin.details.model.PluginConfigKeyAllowedValue;
import com.funnelback.plugin.details.model.PluginConfigKeyEncrypted;
import com.funnelback.plugin.details.model.PluginConfigKeyType;
import com.funnelback.plugin.details.model.PluginTarget;
import com.funnelback.plugin.docs.model.ProductSubtopic;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SchemaGeneratorTest {
    @TempDir private Path tmpFolder;

    private static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(PluginUtilsStub.builder()
                    .pluginId("test-id")
                    .pluginName("test")
                    .pluginDescription("test desc")
                    .configKeys(List.of()).build(),
                "{\"configFiles\":null,\"pluginName\":\"test\",\"pluginId\":\"test-id\",\"pluginDescription\":\"test desc\",\"configKeys\":[],\"metadataTags\":null,\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
                "Basic"),
            Arguments.of(PluginUtilsStub.builder()
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
                            .longDescription("longDesc2")
                            .required(true).build())).build(),
                "{\"configFiles\":null,\"pluginName\":null,\"pluginId\":null,\"pluginDescription\":null,\"configKeys\":[{\"defaultValue\":2,\"showIfKeyHasValue\":null,\"description\":\"desc1\",\"longDescription\":null,\"id\":\"int.*\",\"allowedValue\":{\"regex\":null,\"values\":[1,2,3],\"type\":\"FIXED_LIST\"},\"label\":\"key1\",\"type\":{\"subtype\":null,\"type\":\"INTEGER\"},\"key\":\"plugin.test.config.int.*\",\"required\":false},{\"defaultValue\":null,\"showIfKeyHasValue\":null,\"description\":\"desc2\",\"longDescription\":\"longDesc2\",\"id\":\"pass\",\"allowedValue\":null,\"label\":\"key2\",\"type\":{\"subtype\":null,\"type\":\"PASSWORD\"},\"key\":\"plugin.test.encrypted.pass\",\"required\":true}],\"metadataTags\":null,\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
                "With config keys"),
            Arguments.of(PluginUtilsStub.builder()
                    .configFiles(List.of(PluginConfigFile.builder()
                        .name("rules.cfg")
                        .label("rules")
                        .description("desc")
                        .format("json")
                        .required(true).build())).build(),
                "{\"configFiles\":[{\"format\":\"json\",\"name\":\"rules.cfg\",\"description\":\"desc\",\"label\":\"rules\",\"required\":true}],\"pluginName\":null,\"pluginId\":null,\"pluginDescription\":null,\"configKeys\":null,\"metadataTags\":null,\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
                "With config file"),
            Arguments.of(PluginUtilsStub.builder()
                    .pluginTarget(List.of(PluginTarget.DATA_SOURCE, PluginTarget.RESULTS_PAGE)).build(),
                "{\"configFiles\":null,\"pluginName\":null,\"pluginId\":null,\"pluginDescription\":null,\"configKeys\":null,\"metadataTags\":null,\"pluginTarget\":[\"Data source\",\"Results page\"],\"filterClass\":null,\"jsoupFilterClass\":null}",
                "With plugin targets (scope)"),
            Arguments.of(PluginUtilsStub.builder()
                    .productSubtopic(List.of(ProductSubtopic.Indexing.DOCUMENT_FILTERS, ProductSubtopic.ResultsPage.BEST_BETS)).build(),
                "{\"configFiles\":null,\"pluginName\":null,\"pluginId\":null,\"pluginDescription\":null,\"configKeys\":null,\"metadataTags\":[\"Document filters\",\"Best bets\"],\"pluginTarget\":null,\"filterClass\":null,\"jsoupFilterClass\":null}",
                "With metadata tags")
        );
    }

    @DisplayName("Generating schema")
    @ParameterizedTest(name = "{index}: {2}")
    @MethodSource("data")
    public void test(PluginUtilsBase pluginUtils, String expectedSchema, String desc) throws IOException, JSONException {
        new SchemaGenerator(pluginUtils, tmpFolder.toFile().getCanonicalPath()).generate();
        String actual = Files.readString(tmpFolder.resolve(SchemaGenerator.FILE_NAME));
        JSONAssert.assertEquals(expectedSchema, actual, true);
    }

}