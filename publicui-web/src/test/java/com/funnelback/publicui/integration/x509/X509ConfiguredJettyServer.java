package com.funnelback.publicui.integration.x509;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import com.funnelback.common.config.Config;

/**
 * Starts and stops a Jetty server configured for X.509 client certificate authentication.
 * 
 * WARNING - This has to mess with the Config.SYSPROP_INSTALL_DIR system property.
 * It resets it at the end, but that may be a problem if other tests run in parallel.
 */
public class X509ConfiguredJettyServer {
    private Server server;
    private String priorInstallDirPropertyValue;
    private File searchHome;
    
    public X509ConfiguredJettyServer(File searchHome) {
        this.searchHome = searchHome;
    }

    public void start() throws Exception {
        priorInstallDirPropertyValue = System.getProperty(Config.SYSPROP_INSTALL_DIR);
        System.setProperty(Config.SYSPROP_INSTALL_DIR, searchHome.getAbsolutePath());
        
        this.server = createServer();
        this.server.start();
    }

    public void stop() throws Exception {
        this.server.stop();
        this.server.join();
        this.server.destroy();
        
        if (priorInstallDirPropertyValue == null) {
            System.getProperties().remove(Config.SYSPROP_INSTALL_DIR);
        } else {
            System.setProperty(Config.SYSPROP_INSTALL_DIR, priorInstallDirPropertyValue);
        }
    }

    private Server createServer() throws NamingException {
        final Server server = new Server();

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(new File("src/test/resources/x509/keystores/server-keystore.jks").getAbsolutePath());
        sslContextFactory.setKeyStorePassword("funnelback");
        sslContextFactory.setKeyManagerPassword("funnelback");
        sslContextFactory.setTrustStorePath(new File("src/test/resources/x509/keystores/server-truststore.jks").getAbsolutePath());
        sslContextFactory.setTrustStorePassword("funnelback");
        sslContextFactory.setWantClientAuth(true);
        sslContextFactory.setNeedClientAuth(false);

        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        // SSL Connector
        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(https_config));
        sslConnector.setPort(8443);
        server.addConnector(sslConnector);
        
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/s");
        context.setResourceBase(new File("src/main/webapp").getAbsolutePath());
        context.setDescriptor(new File("src/main/webapp/WEB-INF/web.xml").getAbsolutePath());
        context.setParentLoaderPriority(true);
        context.setServer(server);
        
        new Resource(null, "java:comp/env/userSaltMap", new ConcurrentHashMap());
        // Just creating that magically shoves it into some global context

        server.setHandler(context);

        return server;
    }
    
    public String getBaseUrl() {
        return "https://localhost:8443";
    }
}
