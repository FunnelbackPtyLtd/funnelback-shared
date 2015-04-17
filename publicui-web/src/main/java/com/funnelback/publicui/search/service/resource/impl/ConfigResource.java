package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j2;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Files.Types;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.springmvc.service.resource.ParseableResource;

/**
 * Parses a collection configuration files into a {@link Config}
 */
@Log4j2
public class ConfigResource implements ParseableResource<Config> {

    private final File searchHome;
    private final String collectionId;
    private final File configFile;

    public ConfigResource(File searchHome, String collectionId) {
        this.searchHome = searchHome;
        this.collectionId = collectionId;
        this.configFile = new File(searchHome
                + File.separator + DefaultValues.FOLDER_CONF
                + File.separator + collectionId,
                Files.COLLECTION_FILENAME);
    }
    
    @Override
    public Config parse() throws IOException {
        log.debug("Creating config object for collection '"+collectionId+"'");
        return new NoOptionsConfig(searchHome, collectionId);
    }

    @Override
    public boolean isStale(long timestamp) {
        for(Types type: Types.values()) {
            if (Config.getLastUpdated(searchHome, type, collectionId) > timestamp) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Object getCacheKey() {
        return configFile;
    }
    
    @Override
    public boolean exists() {
        return configFile.exists();
    }

}
