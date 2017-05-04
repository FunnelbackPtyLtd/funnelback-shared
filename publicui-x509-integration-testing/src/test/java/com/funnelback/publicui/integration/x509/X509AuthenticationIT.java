package com.funnelback.publicui.integration.x509;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.PrivateKeyDetails;
import org.apache.http.conn.ssl.PrivateKeyStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

public class X509AuthenticationIT {

    // The easiest way I've found to hand-run this for development
    // is to run `mvn -Dmaven.failsafe.debug verify` on the command
    // line which will start the jetty instance and then wait for
    // a debug connection (which you can ignore and instead manually
    // run the test from within eclipse)
    
    @Test
    public void testX509Auth() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, UnrecoverableKeyException {
        String responseText = performTestRequest(Optional.of(KEYSTORE_PATH));
        Assert.assertEquals("Expected access to be granted", "Access granted!\n", responseText);
    }

    @Test(expected=SSLHandshakeException.class)
    public void testX509AuthUntrusted() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, UnrecoverableKeyException {
        String responseText = performTestRequest(Optional.of(UNTRUSTED_KEYSTORE_PATH));
    }

    @Test
    public void testNoClientCertificate() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, UnrecoverableKeyException {
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

    private String performTestRequest(Optional<String> keystorePath) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, UnrecoverableKeyException {
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
        HttpGet get = new HttpGet(X509AuthenticationIT.TEST_URL);

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();

            String responseText = IOUtils.toString(entity.getContent());
            EntityUtils.consume(entity);

            return responseText;
        }
    }

}
