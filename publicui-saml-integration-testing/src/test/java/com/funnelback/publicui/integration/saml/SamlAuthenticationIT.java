package com.funnelback.publicui.integration.saml;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SamlAuthenticationIT {

    // The easiest way I've found to hand-run this for development
    // is to run `mvn -Dmaven.failsafe.debug verify` on the command
    // line which will start the jetty instance and then wait for
    // a debug connection (which you can ignore and instead manually
    // run the test from within eclipse)

    private static String testBaseUrl = "http://localhost:8084";
    private static String mujinaBaseUrl = "http://localhost:8080";

    @BeforeClass
    public static void setupProperties() {
        testBaseUrl = System.getProperty("testBaseUrl", testBaseUrl);
        mujinaBaseUrl = System.getProperty("mujinaBaseUrl", mujinaBaseUrl);
    }

    @Test
    public void testInitialRedirect() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(testBaseUrl + "/s/search.html");

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            Assert.assertTrue("Expected to be redirected to the IdP login page",
                responseText.contains("Login page") && responseText.contains("Mujina Identity Provider"));

        }
    }

    @Test
    public void testProactiveHttpBasic() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet get = new HttpGet(testBaseUrl + "/s/search.html");
        get.addHeader("Authorization", "Basic YWRtaW46YWRtaW4="); // That's admin:admin

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            Assert.assertTrue("Expected to get the collection listing page with proactive http basic",
                responseText.contains("Welcome to the Funnelback search service"));
        }

        HttpGet get2 = new HttpGet(testBaseUrl + "/s/search.html");
        // Do it again with no authorization header now - The JSESSIONID cookie should suffice

        try (CloseableHttpResponse response = httpclient.execute(get2)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            Assert.assertTrue("Expected to get the collection listing page with a JSESSIONID cookie",
                responseText.contains("Welcome to the Funnelback search service"));
        }
}

    @Test
    public void testTokenAuth() throws Exception {
        // TODO - How can I get a token (I don't have admin-api to log in)
    }

    @Test
    public void testFullSamlAuth() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom()
            .setRedirectStrategy(new LaxRedirectStrategy())
            // We want to follow a POST redirect to a GET - see http://stackoverflow.com/a/23181680/797
            .build();

        HttpGet getLoginPage = new HttpGet(testBaseUrl + "/s/search.html");

        try (CloseableHttpResponse response = httpclient.execute(getLoginPage)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            Assert.assertTrue("Expected to be redirected to the IdP login page",
                responseText.contains("Login page") && responseText.contains("Mujina Identity Provider"));
        }

        HttpPost postLoginForm = new HttpPost(mujinaBaseUrl + "/login");

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

            Assert.assertTrue("Expected to reach the Funnelback collection listing page after SAML authentication",
                responseText.contains("Welcome to the Funnelback search service"));
        }

    }

}
