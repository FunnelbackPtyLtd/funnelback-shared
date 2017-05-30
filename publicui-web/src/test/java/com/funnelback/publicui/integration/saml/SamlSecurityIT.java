package com.funnelback.publicui.integration.saml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SamlSecurityIT {
    private static SamlConfiguredJettyServer server;
    private static MujinaIdentityProviderServer mujina;
    private static File searchHome = new File("src/test/resources/saml/search-home");

    @BeforeClass
    public static void startServers() throws Exception {
        SamlSecurityIT.mujina = new MujinaIdentityProviderServer();
        SamlSecurityIT.mujina.start();
        
        SamlSecurityIT.server = new SamlConfiguredJettyServer(searchHome);
        SamlSecurityIT.server.start();
    }

    @Test
    public void testProactiveHttpBasic() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(SamlSecurityIT.server.getBaseUrl() + "/s/search.html");
        get.addHeader("Authorization", "Basic YWRtaW46YWRtaW4="); // That's admin:admin

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);
            
            Assert.assertThat("Expected to reach the Funnelback collection listing page after proactive HTTP Basic authentication", responseText,
                StringContains.containsString("Access granted!"));
        }

        HttpGet get2 = new HttpGet(SamlSecurityIT.server.getBaseUrl() + "/s/search.html");
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
        
        HttpGet get = new HttpGet(SamlSecurityIT.server.getBaseUrl() + "/s/search.html");
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

        HttpGet getLoginPage = new HttpGet(SamlSecurityIT.server.getBaseUrl() + "/s/search.html");

        try (CloseableHttpResponse response = httpclient.execute(getLoginPage)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            Assert.assertThat("Expected to be redirected to the IdP login page", responseText,
                AllOf.allOf(StringContains.containsString("Login page"), StringContains.containsString("Mujina Identity Provider")));
        }

        HttpPost postLoginForm = new HttpPost(SamlSecurityIT.mujina.getBaseUrl() + "/login");

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