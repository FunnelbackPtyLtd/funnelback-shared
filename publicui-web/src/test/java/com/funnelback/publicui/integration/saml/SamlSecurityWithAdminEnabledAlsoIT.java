package com.funnelback.publicui.integration.saml;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.springframework.util.SocketUtils;

import com.funnelback.common.testutils.TmpFolderProvider;
import com.funnelback.springmvc.utils.saml.MujinaIdentityProviderServer;
import com.funnelback.springmvc.utils.security.DefaultSecurityConfiguredJettyServer;
import com.google.common.io.Files;

public class SamlSecurityWithAdminEnabledAlsoIT extends SamlSecurityIT {

    @BeforeClass
    public static void startServers() throws Exception {
        SamlSecurityIT.mujina = new MujinaIdentityProviderServer();
        SamlSecurityIT.mujina.start(TmpFolderProvider.getTmpDir(SamlSecurityWithAdminEnabledAlsoIT.class, "all", "mujina"));
        
        Integer port = SocketUtils.findAvailableTcpPort();
        File searchHome = createSearchHome(port);

        // The test is basically that none of these settings matter, because we're the public publicui in this case
        File globalCfgDefault = new File(searchHome, "conf" + File.separator + "global.cfg.default");
        Files.append("\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.enabled=true\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.identity-provider-metadata-url=http://not-a-real-place.com/\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.entity-id-prefix=com:funnelback:admin:sp\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.keystore-path=does-not-exist\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.keystore-password=foo\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.key-alias=bar\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.key-password=bar\n", globalCfgDefault, StandardCharsets.UTF_8);
        Files.append("auth.admin.saml.groovy-permission-mapper=doesntmattercauseitshouldnotberead.groovy\n", globalCfgDefault, StandardCharsets.UTF_8);
        
        SamlSecurityIT.searchHome = searchHome;
        
        SamlSecurityIT.server = new DefaultSecurityConfiguredJettyServer(searchHome, "/s", port);
        SamlSecurityIT.server.start();
    }

    // All the actual tests should be inherited form the parent!
    
    @AfterClass
    public static void stopServers() throws Exception {
        SamlSecurityIT.server.stop();
        SamlSecurityIT.mujina.stop();
    }
}
