package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.funnelback.common.Security;
import com.funnelback.common.Security.System;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.net.TrustAllCertsX509TrustManager;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>Pass user keys based on a "userkeys" request parameter, which would normally be added
 * by some external system which is wrapping the search results (a portal).</p>
 * 
 * <p>Note that this approach is not secure unless Funnelback can be accessed only via
 * the portal.</p>
 */
public class ManifoldCFMapper implements UserKeysMapper {

    /** Name of the URL parameter to use to pass the username 
     * (TODO - Remove this and use the remote user, it's just for testing
     * right now.) */
    public static final String USERNAME_PARAMETER_NAME = "user";

    private final static String SERVICE_USERNAME = "_svc_manifoldcf";
    
    public ManifoldCFMapper() throws NoSuchAlgorithmException, KeyManagementException {
        // Arrange to trust all SSL certificates (because the admin UI usually has a self signed one
        SSLContext sc = SSLContext.getInstance("SSL");
        TrustManager[] trustAllCerts = new TrustManager[] {new TrustAllCertsX509TrustManager()};
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        allHostsValidVerifier = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
              return true;
            }
        };
    }
    
    private HostnameVerifier allHostsValidVerifier;
    
    @Override
    public List<String> getUserKeys(Collection currentCollection, SearchTransaction transaction) {
        try {
            String username = transaction.getQuestion().getInputParameterMap().get(USERNAME_PARAMETER_NAME);
            // TODO - Get the remote user instead somehow
            
            Config config = transaction.getQuestion().getCollection().getConfiguration();
            
            String domain = config.value(Keys.ManifoldCF.DOMAIN);

            String authority = config.value(Keys.ManifoldCF.AUTHORITY_URL_PREFIX);
            URL authorityUrl = new URL(authority + "/UserACLs?username=" + username + "@" + domain);
            
            HttpURLConnection connection = (HttpURLConnection) authorityUrl.openConnection();
            
            if (connection instanceof HttpsURLConnection) {
                // Ignore invalid HTTPS certificates
                ((HttpsURLConnection) connection).setHostnameVerifier(allHostsValidVerifier);
            }
            
            String servicePassword = Security.generateSystemPassword(System.MANIFOLDCF, config.value(Keys.SERVER_SECRET));
            String usernameAndPassword = SERVICE_USERNAME + ":" + servicePassword;
            String basicAuth = "Basic " + new String(new Base64().encode(usernameAndPassword.getBytes()));
            connection.setRequestProperty("Authorization", basicAuth);
            
            InputStream in = connection.getInputStream();
            
            String authorityInfo;
            try {
                authorityInfo = IOUtils.toString(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
            
            List<String> result = getKeysFromAuthorityInfo(authorityInfo);
            
            return result;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getKeysFromAuthorityInfo(String authorityInfo) {
        List<String> result = new ArrayList<String>();
        if (authorityInfo != null) {
            for (String line: authorityInfo.split("\n")) {
                String[] fields = line.split(":");
                
                if (fields.length == 3 && "TOKEN".equals(fields[0])) {
                    result.add(fields[2]);
                } else if (fields.length == 2 && "AUTHORIZED".equals(fields[0])) {
                    // Not sure what to do with these
                } else if (fields.length == 2 && "USERNOTFOUND".equals(fields[0])) {
                    // No keys for unknown users
                } else {
                    // TODO - Work out what other cases are possible and handle them
                    throw new RuntimeException("Unrecognised authority output: " + line);
                }
            }
        }
        return result;
    }
}