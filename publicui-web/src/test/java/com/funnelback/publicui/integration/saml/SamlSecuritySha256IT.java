package com.funnelback.publicui.integration.saml;

import okhttp3.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import org.hamcrest.core.StringContains;
import org.springframework.util.SocketUtils;

import com.funnelback.common.testutils.SearchHomeConfigs;
import com.funnelback.common.testutils.SearchHomeProvider;
import com.funnelback.common.testutils.TmpFolderProvider;
import com.funnelback.publicui.integration.DefaultAdminSecurityIT;
import com.funnelback.springmvc.utils.saml.MujinaIdentityProviderServer;
import com.funnelback.springmvc.utils.security.DefaultSecurityConfiguredJettyServer;

public class SamlSecuritySha256IT {
    protected static DefaultSecurityConfiguredJettyServer server;
    protected static MujinaIdentityProviderServer mujina;
    protected static File searchHome;

    @BeforeClass
    public static void startServers() throws Exception {
        SamlSecuritySha256IT.mujina = new MujinaIdentityProviderServer();
        SamlSecuritySha256IT.mujina.start(TmpFolderProvider.getTmpDir(SamlSecuritySha256IT.class, "all", "mujina"));

        Integer port = SocketUtils.findAvailableTcpPort();
        searchHome = createSearchHome(port);

        SamlSecuritySha256IT.server = new DefaultSecurityConfiguredJettyServer(searchHome, "/s", port);
        SamlSecuritySha256IT.server.start();
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
        searchHomeConfigs.getGlobalCfgDefault().put("auth.saml.algorithm", "sha256");
        searchHomeConfigs.getGlobalCfgDefault().put("urls.search_port", port.toString());

        File searchHome = SearchHomeProvider.getNamedWritableSearchHomeForTestClass(SamlSecuritySha256IT.class, searchHomeConfigs, "SAML-Server");

        DefaultSecurityConfiguredJettyServer.basicSearchHomeSetupForServer(searchHome);
        DefaultAdminSecurityIT.commonPublicUISearchHomeSetup(searchHome);

        return searchHome;
    }

    @Test
    public void testSaml() throws IOException {
        try (Response response = MujinaIdentityProviderServer.testSamlLogin(
            server, mujina, server.getBaseUrl() + "search.html",
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256")) {
            Assert.assertThat("Expected to reach the Funnelback collection listing page after SAML authentication",
                response.body().string(),
                StringContains.containsString("Access granted!"));
        }
    }

    @AfterClass
    public static void stopServers() throws Exception {
        SamlSecuritySha256IT.server.stop();
        SamlSecuritySha256IT.mujina.stop();
    }
}
