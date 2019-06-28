package com.funnelback.publicui.integration.x509;

import java.util.concurrent.ConcurrentHashMap;

import java.io.File;

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
import com.funnelback.springmvc.api.config.SharedBetweenContainersHelper;

/**
 * Starts and stops a Jetty server configured for X.509 client certificate authentication.
 * 
 * WARNING - This has to mess with the Config.SYSPROP_INSTALL_DIR system property.
 * It resets it at the end, but that may be a problem if other tests run in parallel.
 */
public class X509ConfiguredJettyServer {
    private Server server;
    ServerConnector sslConnector;
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
        // Setting this to resolve a X509 related problem when we upgraded to 9.4.19
        // @see https://github.com/eclipse/jetty.project/issues/3656
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        HttpConfiguration https_config = new HttpConfiguration(new HttpConfiguration());
        https_config.addCustomizer(new SecureRequestCustomizer());

        // SSL Connector
        sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(https_config));
        sslConnector.setPort(0);
        server.addConnector(sslConnector);
        
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/s");
        context.setResourceBase(new File("src/main/webapp").getAbsolutePath());
        context.setDescriptor(new File("src/main/webapp/WEB-INF/web.xml").getAbsolutePath());
        context.setParentLoaderPriority(true);
        context.setServer(server);
        
        new Resource(null, SharedBetweenContainersHelper.SHARED_MAP_NAME, new ConcurrentHashMap());
        // Just creating that magically shoves it into some global context

        server.setHandler(context);

        return server;
    }
    
    public String getBaseUrl() {
        return "https://localhost:" + sslConnector.getLocalPort();
    }
}
