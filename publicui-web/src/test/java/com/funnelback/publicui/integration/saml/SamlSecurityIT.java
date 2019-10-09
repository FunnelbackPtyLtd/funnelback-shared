package com.funnelback.publicui.integration.saml;

import okhttp3.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hamcrest.core.StringContains;
import org.springframework.util.SocketUtils;

import com.funnelback.common.testutils.SearchHomeConfigs;
import com.funnelback.common.testutils.SearchHomeProvider;
import com.funnelback.common.testutils.TmpFolderProvider;
import com.funnelback.publicui.integration.DefaultAdminSecurityIT;
import com.funnelback.springmvc.api.config.SharedBetweenContainersHelper;
import com.funnelback.springmvc.api.config.security.SecurityConfigBase;
import com.funnelback.springmvc.utils.saml.MujinaIdentityProviderServer;
import com.funnelback.springmvc.utils.saml.TokenUtils;
import com.funnelback.springmvc.utils.security.DefaultSecurityConfiguredJettyServer;

public class SamlSecurityIT {
    protected static DefaultSecurityConfiguredJettyServer server;
    protected static MujinaIdentityProviderServer mujina;
    protected static File searchHome;

    @BeforeClass
    public static void startServers() throws Exception {
        SamlSecurityIT.mujina = new MujinaIdentityProviderServer();
        SamlSecurityIT.mujina.start(TmpFolderProvider.getTmpDir(SamlSecurityIT.class, "all", "mujina"));

        Integer port = SocketUtils.findAvailableTcpPort();
        searchHome = createSearchHome(port);

        SamlSecurityIT.server = new DefaultSecurityConfiguredJettyServer(searchHome, "/s", port);
        SamlSecurityIT.server.start();
    }

    public static File createSearchHome() throws Exception, IOException {
        return createSearchHome(0);
    }
    
    public static File createSearchHome(Integer port) throws Exception, IOException {
        SearchHomeConfigs searchHomeConfigs = SearchHomeConfigs.getWithDefaults();
        searchHomeConfigs.getGlobalCfgDefault().put("server_secret", "test");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.enabled", "true");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.identity-provider-metadata-url", "http://localhost:" + mujina.getPort() + "/metadata");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.entity-id-prefix", "com:funnelback:publicui:sp");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.keystore-path", new File("src/test/resources/saml/samlKeystore.jks").getAbsolutePath());
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.keystore-password", "nalle123");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.key-alias", "apollo");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.key-password", "nalle123");
        searchHomeConfigs.getGlobalCfgDefault().put("urls.search_port", port.toString());

        File searchHome = SearchHomeProvider.getNamedWritableSearchHomeForTestClass(SamlSecurityIT.class, searchHomeConfigs, "SAML-Server");
        
        DefaultSecurityConfiguredJettyServer.basicSearchHomeSetupForServer(searchHome);
        DefaultAdminSecurityIT.commonPublicUISearchHomeSetup(searchHome);
        
        return searchHome;
    }

    @Test
    public void testProactiveHttpBasic() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(server.getBaseUrl() + "search.html");
        get.addHeader("Authorization", "Basic YWRtaW46YWRtaW4="); // That's admin:admin

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);
            
            Assert.assertThat("Expected to reach the Funnelback collection listing page after proactive HTTP Basic authentication", responseText,
                StringContains.containsString("Access granted!"));
        }

        HttpGet get2 = new HttpGet(server.getBaseUrl() + "search.html");
        // Do it again with no authorization header now - The JSESSIONID cookie should suffice

        try (CloseableHttpResponse response = httpclient.execute(get2)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            Assert.assertThat("Expected to get the collection listing page with a JSESSIONID cookie", responseText,
                StringContains.containsString("Access granted!"));
        }
    }

    @Test
    public void testTokenAuth() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();
        
        new SharedBetweenContainersHelper()
            .getSharedConcurrentHashMap(SecurityConfigBase.USER_SALT_MAP_KEY)
                .put("admin", "dummyHash");
        String token = TokenUtils.makeToken(searchHome, "test", "admin", "dummyHash");
        
        HttpGet get = new HttpGet(server.getBaseUrl() + "search.html");
        get.addHeader("X-Security-Token", token); // That's admin:admin

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);
            
            Assert.assertThat("Expected to get the collection listing page with an X-Security-Token header", responseText,
                StringContains.containsString("Access granted!"));
        }
    }

    @Test
    public void testSaml() throws IOException {
        try (Response response = MujinaIdentityProviderServer.testSamlLogin(
            server, mujina, server.getBaseUrl() + "search.html",
            "http://www.w3.org/2000/09/xmldsig#rsa-sha1")) {
            Assert.assertThat("Expected to reach the Funnelback collection listing page after SAML authentication",
                response.body().string(),
                StringContains.containsString("Access granted!"));
        }
    }

    @Test
    public void testFetchMetadata() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(server.getBaseUrl() + "saml/metadata");

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);
            
            Assert.assertThat("Expected to get the SAML metadata", responseText,
                StringContains.containsString("X509Certificate"));
        }
    }
    
    @AfterClass
    public static void stopServers() throws Exception {
        SamlSecurityIT.server.stop();
        SamlSecurityIT.mujina.stop();
    }
}
