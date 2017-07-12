package com.funnelback.publicui.integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

import com.funnelback.common.testutils.SearchHomeConfigs;
import com.funnelback.common.testutils.SearchHomeProvider;
import com.funnelback.springmvc.utils.saml.MujinaIdentityProviderServer;
import com.funnelback.springmvc.utils.saml.TokenUtils;
import com.funnelback.springmvc.utils.security.DefaultSecurityConfiguredJettyServer;

public class DefaultAdminSecurityIT {
    protected static DefaultSecurityConfiguredJettyServer server;
    protected static File searchHome;

    @BeforeClass
    public static void startServers() throws Exception {
        searchHome = createSearchHome();

        DefaultAdminSecurityIT.server = new DefaultSecurityConfiguredJettyServer(searchHome, "/s");
        DefaultAdminSecurityIT.server.getExtraContextAttributes().put("ExecutionContext", "Admin");
        DefaultAdminSecurityIT.server.start();
    }

    public static File createSearchHome() throws Exception, IOException {
        SearchHomeConfigs searchHomeConfigs = SearchHomeConfigs.getWithDefaults();
        searchHomeConfigs.getGlobalCfgDefault().put("server_secret", "test");

        File searchHome = SearchHomeProvider.getNamedWritableSearchHomeForTestClass(DefaultAdminSecurityIT.class, searchHomeConfigs, "Normal-Admin-Server");
        
        DefaultSecurityConfiguredJettyServer.basicSearchHomeSetupForServer(searchHome);

        File modernUiProperties = new File(searchHome, "web/conf/modernui/modernui.properties");
        modernUiProperties.getParentFile().mkdirs();
        FileUtils.write(modernUiProperties, "");

        File versionFile = new File(searchHome, "VERSION/funnelback-release");
        versionFile.getParentFile().mkdirs();
        FileUtils.write(versionFile, "Funnelback 9.9.9\n");

        File noCollectionFile = new File(searchHome, "web/templates/modernui/no-collection.ftl");
        noCollectionFile.getParentFile().mkdirs();
        FileUtils.write(noCollectionFile, "Access granted!\n");
        
        return searchHome;
    }

    @Test
    public void testNoAuth() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(server.getBaseUrl() + "search.html");

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            Assert.assertEquals(401,  response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testHttpBasic() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(server.getBaseUrl() + "search.html");
        get.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);
            
            Assert.assertThat("Expected to reach the Funnelback collection listing page after proactive HTTP Basic authentication", responseText,
                StringContains.containsString("Access granted!"));
        }
    }

    @Test
    public void testInvalidHttpBasicUser() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(server.getBaseUrl() + "search.html");
        get.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("notarealuser:wrongpassword".getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            // FUN-10496 was causing a 500 here as we tried to read a missing ini file
            Assert.assertEquals(401,  response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testTokenAuth() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

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
    
    @AfterClass
    public static void stopServers() throws Exception {
        DefaultAdminSecurityIT.server.stop();
    }
}
