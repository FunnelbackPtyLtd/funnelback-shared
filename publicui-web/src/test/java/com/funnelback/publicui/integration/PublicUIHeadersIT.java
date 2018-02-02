package com.funnelback.publicui.integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.StringContains;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.funnelback.common.system.Security;
import com.funnelback.common.testutils.SearchHomeConfigs;
import com.funnelback.common.testutils.SearchHomeProvider;
import com.funnelback.springmvc.utils.saml.MujinaIdentityProviderServer;
import com.funnelback.springmvc.utils.saml.TokenUtils;
import com.funnelback.springmvc.utils.security.DefaultSecurityConfiguredJettyServer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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

    public static void commonPublicUISearchHomeSetup(File searchHome) throws IOException {
        File modernUiProperties = new File(searchHome, "web/conf/modernui/modernui.properties");
        modernUiProperties.getParentFile().mkdirs();
        FileUtils.write(modernUiProperties, "");

        File versionFile = new File(searchHome, "VERSION/funnelback-release");
        versionFile.getParentFile().mkdirs();
        FileUtils.write(versionFile, "Funnelback 9.9.9\n");

        File noCollectionFile = new File(searchHome, "web/templates/modernui/no-collection.ftl");
        noCollectionFile.getParentFile().mkdirs();
        FileUtils.write(noCollectionFile, "Access granted!\n");
    }

    private static File createSearchHome() throws Exception, IOException {
        SearchHomeConfigs searchHomeConfigs = SearchHomeConfigs.getWithDefaults();
        searchHomeConfigs.getGlobalCfgDefault().put("server_secret", PublicUIHeadersIT.SERVER_SECRET);

        File searchHome = SearchHomeProvider.getNamedWritableSearchHomeForTestClass(PublicUIHeadersIT.class, searchHomeConfigs,
            "Normal-Admin-Server");

        DefaultSecurityConfiguredJettyServer.basicSearchHomeSetupForServer(searchHome);
        PublicUIHeadersIT.commonPublicUISearchHomeSetup(searchHome);

        // Add a service user for testing
        File realmProperties = new File(searchHome, "conf/realm.properties");
        String pw_hash = BCrypt.hashpw(
            Security.generateSystemPassword(com.funnelback.common.system.Security.System.ADMIN_API, PublicUIHeadersIT.SERVER_SECRET),
            BCrypt.gensalt());
        FileUtils.write(realmProperties, Security.getServiceAccountName(com.funnelback.common.system.Security.System.ADMIN_API)
            + ": BCRYPT:" + pw_hash + ",_svc_admin_api,admin\n", true);

        File adminUserIni = new File(searchHome, "admin/users/admin.ini");
        File sampleSuperUserIni = new File(searchHome, "share/sample-super-user.ini.dist");
        sampleSuperUserIni.getParentFile().mkdirs();
        FileUtils.copyFile(adminUserIni, sampleSuperUserIni);

        return searchHome;
    }

    @Test
    public void test() throws Exception {
        OkHttpClient client = new OkHttpClient();

        Map<String, Pattern> expectedHeaders = new HashMap<>();

        /*
         * Before you change or add something to this list (e.g. because you upgraded 
         * spring-security and it started producing some new security headers), think
         * about the impact on existing implementations, and what could be done to get 
         * back to the old behavior if needed.
         * 
         * In the past, "Strict-Transport-Security" got added and started sending things
         * to https that should not have been, and when "X-Frame-Options" it caused trouble
         * for some implementations (See FUN-11249).
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

    @AfterClass
    public static void stopServers() throws Exception {
        PublicUIHeadersIT.server.stop();
    }
}
