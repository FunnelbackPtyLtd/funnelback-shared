package com.funnelback.publicui.search.service.resource.impl;

import java.util.Map;

import java.io.File;
import java.io.IOException;

import com.funnelback.common.config.ConfigReader;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

import lombok.extern.log4j.Log4j2;

/**
 * Parses a Funnelback configuration file and returns the
 * config data in a Map
 *
 */
@Log4j2
public class ConfigMapResource extends AbstractSingleFileResource<Map<String, String>> {

    private final String collectionId;
    private final File searchHome;

    public ConfigMapResource(String collectionId, File searchHome, File configFile) {
        super(configFile);
        this.searchHome = searchHome;
        this.collectionId = collectionId;
    }
    
    public ConfigMapResource(File searchHome, File configFile) {
        super(configFile);
        this.searchHome = searchHome;
        this.collectionId = null;
    }
    
    @Override
    public Map<String, String> parseResourceOnly() throws IOException {
        log.debug("Reading configuration data from '"+file.getAbsolutePath()+"'");
        if (collectionId != null) {
            return ConfigReader.readConfig(file, searchHome, collectionId);
        } else {
            return ConfigReader.readConfig(file, searchHome);
        }
    }    
}
