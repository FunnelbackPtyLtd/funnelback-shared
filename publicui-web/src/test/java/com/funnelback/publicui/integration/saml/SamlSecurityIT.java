package com.funnelback.publicui.integration.saml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import com.funnelback.publicui.integration.DefaultAdminSecurityIT;
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
        SamlSecurityIT.mujina.start();

        searchHome = createSearchHome();

        SamlSecurityIT.server = new DefaultSecurityConfiguredJettyServer(searchHome, "/s");
        SamlSecurityIT.server.start();
    }

    public static File createSearchHome() throws Exception, IOException {
        SearchHomeConfigs searchHomeConfigs = SearchHomeConfigs.getWithDefaults();
        searchHomeConfigs.getGlobalCfgDefault().put("server_secret", "test");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.enabled", "true");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.identity-provider-metadata-url", "http://localhost:8080/metadata");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.entity-id", "com:funnelback:publicui:sp");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.keystore-path", new File("src/test/resources/saml/samlKeystore.jks").getAbsolutePath());
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.keystore-password", "nalle123");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.key-alias", "apollo");
        searchHomeConfigs.getGlobalCfgDefault().put("auth.publicui.saml.key-password", "nalle123");

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
    public void testSamlAuth() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom()
            .setRedirectStrategy(new LaxRedirectStrategy())
            // We want to follow a POST redirect to a GET - see http://stackoverflow.com/a/23181680/797
            .build();

        HttpGet getLoginPage = new HttpGet(server.getBaseUrl() + "search.html");

        try (CloseableHttpResponse response = httpclient.execute(getLoginPage)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            Assert.assertThat("Expected to be redirected to the IdP login page", responseText,
                AllOf.allOf(StringContains.containsString("Login page"), StringContains.containsString("Mujina Identity Provider")));
        }

        HttpPost postLoginForm = new HttpPost(mujina.getBaseUrl() + "/login");

        List<NameValuePair> loginFormParameters = new ArrayList<>();
        loginFormParameters.add(new BasicNameValuePair("username", "admin"));
        loginFormParameters.add(new BasicNameValuePair("password", "secret"));

        postLoginForm.setEntity(new UrlEncodedFormEntity(loginFormParameters));

        List<NameValuePair> confirmFormParameters = new ArrayList<>();
        String targetUrl;
        try (CloseableHttpResponse response = httpclient.execute(postLoginForm)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);
            
            Document doc = Jsoup.parse(responseText);
            
            // Likely very fragile to changes in Mujina (but that probably doesn't change often)
            targetUrl = doc.select("form").first().attr("action");
            confirmFormParameters.add(new BasicNameValuePair("SAMLResponse", doc.select("input[name='SAMLResponse']").first().attr("value")));
            confirmFormParameters.add(new BasicNameValuePair("Signature", doc.select("input[name='Signature']").first().attr("value")));
            confirmFormParameters.add(new BasicNameValuePair("SigAlg", doc.select("input[name='SigAlg']").first().attr("value")));
            confirmFormParameters.add(new BasicNameValuePair("KeyInfo", doc.select("input[name='KeyInfo']").first().attr("value")));
        }

        HttpPost confirmForm = new HttpPost(targetUrl);
        confirmForm.setEntity(new UrlEncodedFormEntity(confirmFormParameters));
        
        try (CloseableHttpResponse response = httpclient.execute(confirmForm)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            Assert.assertThat("Expected to reach the Funnelback collection listing page after SAML authentication", responseText,
                StringContains.containsString("Access granted!"));
        }
    }
    
    @AfterClass
    public static void stopServers() throws Exception {
        SamlSecurityIT.server.stop();
        SamlSecurityIT.mujina.stop();
    }
}
