package com.funnelback.publicui.integration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.funnelback.common.testutils.CollectionProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.funnelback.common.testutils.SearchHomeConfigs;
import com.funnelback.common.testutils.SearchHomeProvider;
import com.funnelback.springmvc.utils.security.DefaultSecurityConfiguredJettyServer;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PublicUIHeadersIT {
    private static final String SERVER_SECRET = "autotest-server-secret";
    protected static DefaultSecurityConfiguredJettyServer server;
    protected static File searchHome;

    @BeforeClass
    public static void startServers() throws Exception {
        searchHome = createSearchHome();

        PublicUIHeadersIT.server = new DefaultSecurityConfiguredJettyServer(searchHome, "/s");
        PublicUIHeadersIT.server.getExtraContextAttributes().put("ExecutionContext", "Public");
        PublicUIHeadersIT.server.start();
    }

    private static File createSearchHome() throws Exception, IOException {
        SearchHomeConfigs searchHomeConfigs = SearchHomeConfigs.getWithDefaults();
        searchHomeConfigs.getGlobalCfgDefault().put("server_secret", PublicUIHeadersIT.SERVER_SECRET);

        File searchHome = SearchHomeProvider.getNamedWritableSearchHomeForTestClass(PublicUIHeadersIT.class, searchHomeConfigs,
            "Normal-Public-Server");

        DefaultSecurityConfiguredJettyServer.basicSearchHomeSetupForServer(searchHome);
        DefaultAdminSecurityIT.commonPublicUISearchHomeSetup(searchHome);

        return searchHome;
    }

    @Test
    public void test() throws Exception {
        OkHttpClient client = new OkHttpClient();

        Map<String, Pattern> expectedHeaders = new HashMap<>();

        /*
         * Before you change or add something to this list (e.g. because Funnelback
         * started returning some new header by default), think about the impact on
         * existing implementations, and what could be done to get back to the old
         * behavior if needed.
         * 
         * In the past, "Strict-Transport-Security" got added and started sending things
         * to https that should not have been, and when "X-Frame-Options"  was added it
         * caused trouble for some implementation upgrades (See FUN-11249, RNDSUPPORT-3048).
         * 
         * At a minimum, a new type of header being produced by default probably warrants
         * something in the release notes!
         */
        expectedHeaders.put("Date", Pattern.compile(".*"));
        expectedHeaders.put("X-Content-Type-Options", Pattern.compile(Pattern.quote("nosniff")));
        expectedHeaders.put("X-XSS-Protection", Pattern.compile(Pattern.quote("1; mode=block")));
        expectedHeaders.put("Cache-Control", Pattern.compile(Pattern.quote("no-cache, no-store, max-age=0, must-revalidate")));
        expectedHeaders.put("Pragma", Pattern.compile(Pattern.quote("no-cache")));
        expectedHeaders.put("Expires", Pattern.compile(Pattern.quote("0")));
        expectedHeaders.put("X-Frame-Options", Pattern.compile(Pattern.quote("DENY")));
        expectedHeaders.put("Content-Language", Pattern.compile(".*"));
        expectedHeaders.put("Content-Type", Pattern.compile(".*"));
        expectedHeaders.put("Transfer-Encoding", Pattern.compile(".*"));
        expectedHeaders.put("Server", Pattern.compile(".*"));
        
        Request request = new Request.Builder().url(server.getBaseUrl() + "search.html").build();

        try (Response response = client.newCall(request).execute()) {
            Headers responseHeaders = response.headers();

            Assert.assertEquals(expectedHeaders.keySet(), responseHeaders.names());

            for (String headerName : responseHeaders.names()) {
                Pattern pattern = expectedHeaders.get(headerName);
                List<String> headerValues = responseHeaders.values(headerName);

                for (String headerValue : headerValues) {
                    Assert.assertTrue("Expected " + headerValue + " from header " + headerName + " to match " + pattern.toString(),
                        pattern.matcher(headerValue).matches());
                }
            }
        }
    }

    @Test
    public void testIframeConfigurable() throws Exception {
        CollectionProvider.createCollection(searchHome, "iframe-test",
            Map.of("ui.modern.form.simple.remove-headers", "X-Frame-Options"));

        Path ftlPath = CollectionProvider.getConfigDir(searchHome, "iframe-test").toPath().resolve("_default/simple.ftl");
        Files.createDirectories(ftlPath.getParent());
        Files.write(ftlPath, "I am a test ftl template".getBytes(StandardCharsets.UTF_8));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(server.getBaseUrl() + "search.html?collection=iframe-test").build();

        try (Response response = client.newCall(request).execute()) {
            Headers responseHeaders = response.headers();

            Assert.assertFalse("Expected X-Frame-Options header to be removed",
                responseHeaders.names().contains("X-Frame-Options"));
        }
    }

    @AfterClass
    public static void stopServers() throws Exception {
        PublicUIHeadersIT.server.stop();
    }
}
