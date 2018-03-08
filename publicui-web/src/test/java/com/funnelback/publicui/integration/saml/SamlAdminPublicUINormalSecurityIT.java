package com.funnelback.publicui.integration.saml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

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
import com.google.common.io.Files;

public class SamlAdminPublicUINormalSecurityIT {
    private static final String SERVER_SECRET = "autotest-server-secret";
    protected static DefaultSecurityConfiguredJettyServer server;
    protected static File searchHome;

    @BeforeClass
    public static void startServers() throws Exception {
        searchHome = createSearchHome();

        // Pretend as if we set up saml just on the public port - This is here to test that these values are ignored by the deployment.
        // It is expected that the tests will fail if these values are not ignored.
        File globalCfgDefault = new File(searchHome, "conf" + File.separator + "global.cfg.default");
        Files.append("\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.enabled=true\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.identity-provider-metadata-url=http://not-a-real-place.com/\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.entity-id-prefix=com:funnelback:admin:sp\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.keystore-path=does-not-exist\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.keystore-password=foo\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.key-alias=bar\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.key-password=bar\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.publicui.saml.enabled=false\n", globalCfgDefault, StandardCharsets.UTF_8);

        SamlAdminPublicUINormalSecurityIT.server = new DefaultSecurityConfiguredJettyServer(searchHome, "/s");
        
        SamlAdminPublicUINormalSecurityIT.server.start();
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
        searchHomeConfigs.getGlobalCfgDefault().put("server_secret", SamlAdminPublicUINormalSecurityIT.SERVER_SECRET);

        File searchHome = SearchHomeProvider.getNamedWritableSearchHomeForTestClass(SamlAdminPublicUINormalSecurityIT.class, searchHomeConfigs, "Normal-Admin-Server");
        
        DefaultSecurityConfiguredJettyServer.basicSearchHomeSetupForServer(searchHome);
        SamlAdminPublicUINormalSecurityIT.commonPublicUISearchHomeSetup(searchHome);

        // Add a service user for testing
        File realmProperties = new File(searchHome, "conf/realm.properties");
        String pw_hash = BCrypt.hashpw(
            Security.generateSystemPassword(com.funnelback.common.system.Security.System.ADMIN_API, SamlAdminPublicUINormalSecurityIT.SERVER_SECRET),
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
    public void testNoAuth() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(server.getBaseUrl() + "search.html");

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            // We expect the 400 error that search.html gives when accessed with no collection
            // if we failed, we'd get a 401 or 5xx type of code.
            Assert.assertEquals(400,  response.getStatusLine().getStatusCode());
        }
    }
    
    @AfterClass
    public static void stopServers() throws Exception {
        SamlAdminPublicUINormalSecurityIT.server.stop();
    }
}
