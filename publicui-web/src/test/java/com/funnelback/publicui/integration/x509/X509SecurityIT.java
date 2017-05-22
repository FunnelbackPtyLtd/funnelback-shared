package com.funnelback.publicui.integration.x509;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.SocketException;
import java.security.KeyStore;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class X509SecurityIT {
    private static X509ConfiguredJettyServer server;
    private static File searchHome = new File("src/test/resources/x509/search-home");

    @BeforeClass
    public static void startServer() throws Exception {
        X509SecurityIT.server = new X509ConfiguredJettyServer(searchHome);
        X509SecurityIT.server.start();
    }

    @Test
    public void testX509Auth() throws Exception {
        String responseText = performTestRequest(Optional.of(KEYSTORE_PATH));
        Assert.assertEquals("Expected access to be granted", "Access granted!\n", responseText);
    }

    @Test
    public void testX509AuthUntrusted() throws Exception {
        try {
            performTestRequest(Optional.of(UNTRUSTED_KEYSTORE_PATH));
        } catch (SSLHandshakeException|SocketException e) {
            return; // That's what we want to happen
        }
        Assert.fail("Expected to get an SSLHandshakeException or SocketException when connecting ");
    }

    @Test
    public void testNoClientCertificate() throws Exception {
        String responseText = performTestRequest(Optional.empty());
        Assert.assertTrue("Expected access to be denied", responseText.contains("Access Denied"));
    }
    
    // Our search_home has a custom template to make this just say "Access Granted!"
    private static final String TEST_URL = "https://localhost:8443/s/search.html";
    
    // src/test/resources/x509/keystores/recreate_keystores.sh is a script which
    // recreates these key/trust stores, and provides a bit of an outline
    // of what it's doing if you need to understand how it works
    
    private static final String KEYSTORE_PATH = "src/test/resources/x509/keystores/client-keystore.jks";
    private static final String KEYSTORE_PASSWORD = "funnelback";
    private static final String KEY_PASSWORD = "funnelback";
    private static final String UNTRUSTED_KEYSTORE_PATH = "src/test/resources/x509/keystores/client-keystore-untrusted.jks";

    private static final String TRUSTSTORE_PATH = "src/test/resources/x509/keystores/client-truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "funnelback";

    private String performTestRequest(Optional<String> keystorePath) throws Exception {
        try {
            // We load in a trust store which trusts the jetty instance's self signed certificate for 'localhost'
            // See https://hc.apache.org/httpcomponents-client-4.3.x/httpclient/examples/org/apache/http/examples/client/ClientCustomSSL.java
            KeyStore trustStore  = KeyStore.getInstance("jks");
            try (InputStream in = new FileInputStream(new File(TRUSTSTORE_PATH))) {
                trustStore.load(in, TRUSTSTORE_PASSWORD.toCharArray());
            }
    
            SSLContextBuilder scb = SSLContexts.custom().loadTrustMaterial(trustStore);
            
            // If we were given a keystore, we load it in - It will provide the client certificate that
            // the jetty server has been configured ot trust
            if (keystorePath.isPresent()) {
                KeyStore keyStore  = KeyStore.getInstance("jks");
                try (InputStream in = new FileInputStream(new File(keystorePath.get()))) {
                    keyStore.load(in, KEYSTORE_PASSWORD.toCharArray());
                }
                
                scb.loadKeyMaterial(keyStore, KEY_PASSWORD.toCharArray());
            }
    
            // The SSLConnection stuff is where the certificate exchange happens
            // SSLContextBuilder.KeyManagerDelegate.chooseClientAlias is a good
            // spot to breakpoint if you need to see the client certificate process
            // in action
            SSLContext sslcontext = scb.build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
    
            // This tells Apache HttpClient to use our custom SSLConnectionSocketFactory
            // for creating SSLConnections (and hence, makes our certificates available.
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslsf)
                .build();
            HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .setConnectionManager(ccm)
                    .build();
    
            // Finally, we can actually make our HTTPS request!
            HttpGet get = new HttpGet(X509SecurityIT.server.getBaseUrl() + "/s/search.html");
            
            try (CloseableHttpResponse response = httpclient.execute(get)) {
                HttpEntity entity = response.getEntity();
    
                String responseText = IOUtils.toString(entity.getContent());
                EntityUtils.consume(entity);
    
                return responseText;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        X509SecurityIT.server.stop();
    }
}
