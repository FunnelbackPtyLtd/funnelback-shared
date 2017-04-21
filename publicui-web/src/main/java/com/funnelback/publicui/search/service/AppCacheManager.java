package com.funnelback.publicui.search.service;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PreDestroy;

import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.utils.web.ExecutionContextHolder;

/**
 * Bean used to construct and shutdown the EhCache instance
 */
@Component
@Log4j2
public class AppCacheManager {
    
    @Autowired
    private ExecutionContextHolder executionContextHolder;
    
    /**
     * @return Returns a CacheManager configured for the current execution context (e.g. Public or Admin)
     * to ensure that the caches for multiple deployments of the war do not clobber each other.
     */
    @Bean
    public CacheManager cacheManager() throws CacheException, IOException {
        try (InputStream cacheConfigInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ehcache-publicui.xml")) {
            Configuration configuration = ConfigurationFactory.parseConfiguration(cacheConfigInputStream);
            
            // Configure the diskStorage - Must be different for different execution contexts (SUPPORT-1347)
            DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
            diskStoreConfiguration.setPath("java.io.tmpdir/EhCache-" + executionContextHolder.getExecutionContext().name());
            configuration.addDiskStore(diskStoreConfiguration);
    
            cacheManager = new CacheManager(configuration);
            
            return cacheManager;
        }
    }

    private CacheManager cacheManager;

    /**
     * Properly shut down EHCache when Spring shuts down.
     * Helps when the app is run under Eclipse / Tomcat
     */
    @PreDestroy
    public void preDestroy() {
        log.debug("Shutting down application cache");
        cacheManager.shutdown();
    }
}
