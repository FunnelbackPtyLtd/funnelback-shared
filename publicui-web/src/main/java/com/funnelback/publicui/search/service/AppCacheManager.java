package com.funnelback.publicui.search.service;

import javax.annotation.PreDestroy;

import lombok.Setter;
import lombok.extern.log4j.Log4j;
import net.sf.ehcache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean used to properly shut down EHCache when Spring shuts
 * down. Helps when the app is run under Eclipse / Tomcat
 */
@Component
@Log4j
public class AppCacheManager {
    
    @Autowired
    @Setter
    private CacheManager appCacheManager;

    /**
     * Shuts EHCache down
     */
    @PreDestroy
    public void preDestroy() {
        log.debug("Shutting down application cache");
        appCacheManager.shutdown();
    }

}
