package com.funnelback.publicui.integration.saml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import com.funnelback.springmvc.utils.saml.MujinaIdentityProviderServer;
import com.funnelback.springmvc.utils.saml.TokenUtils;
import com.funnelback.springmvc.utils.security.DefaultSecurityConfiguredJettyServer;
import com.google.common.io.Files;

public class SamlSecurityWithAdminEnabledAlsoIT extends SamlSecurityIT {

    @BeforeClass
    public static void startServers() throws Exception {
        SamlSecurityIT.mujina = new MujinaIdentityProviderServer();
        SamlSecurityIT.mujina.start();

        File searchHome = createSearchHome();

        File globalCfgDefault = new File(searchHome, "conf" + File.separator + "global.cfg.default");
        Files.append("\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.enabled=true\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.identity-provider-metadata-url=http://localhost:8080/metadata\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.entity-id=com:funnelback:admin:sp\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.keystore-path=" + new File("src/test/resources/saml/samlKeystore.jks").getAbsolutePath() + "\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.keystore-password=nalle123\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.key-alias=apollo\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.enabled=true\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.key-password=nalle123\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.groovy-permission-mapper=doesntmattercauseitshouldnotberead.groovy\n", globalCfgDefault, StandardCharsets.UTF_8);
        
        SamlSecurityIT.searchHome = searchHome;
        
        SamlSecurityIT.server = new DefaultSecurityConfiguredJettyServer(searchHome, "/s");
        SamlSecurityIT.server.start();
    }

    // All the actual tests should be inherited form the parent!
    
    @AfterClass
    public static void stopServers() throws Exception {
        SamlSecurityIT.server.stop();
        SamlSecurityIT.mujina.stop();
    }
}
