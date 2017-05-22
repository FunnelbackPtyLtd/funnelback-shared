package com.funnelback.publicui.integration.saml;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.SpringApplication;

import com.funnelback.common.Environment;
import com.funnelback.common.config.Config;

/**
 * Starts an stops a Jetty server configured for SAML.
 * 
 * WARNING - This has to mess with the Config.SYSPROP_INSTALL_DIR system property.
 * It resets it at the end, but that may be a problem if other tests run in parallel.
 */
public class SamlConfiguredJettyServer {
    private Server server;
    private String priorInstallDirPropertyValue;
    private File searchHome;
    
    public SamlConfiguredJettyServer(File searchHome) {
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
        final Server server = new Server(8084);

        final WebAppContext context = new WebAppContext();
        context.setContextPath("/s");
        context.setResourceBase(new File("src/main/webapp").getAbsolutePath());
        context.setDescriptor(new File("src/main/webapp/WEB-INF/web.xml").getAbsolutePath());
        context.setParentLoaderPriority(true);
        context.setServer(server);
        
        ConcurrentHashMap userSaltMap = new ConcurrentHashMap();
        userSaltMap.put("admin", "dummyHash");
        new Resource(null, "java:comp/env/userSaltMap", userSaltMap);
        // Just creating that magically shoves it into some global context

        server.setHandler(context);

        return server;
    }
    
    public String getBaseUrl() {
        return "http://localhost:8084";
    }
}
