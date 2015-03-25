package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.log4j.Log4j;

import com.funnelback.publicui.json.CuratorConfigParser;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

/**
 * Parses a curator JSON config file and returns a {@link CuratorConfig} object.
 *
 * @see CuratorYamlConfigResource
 */
@Log4j
public class CuratorJsonConfigResource extends AbstractSingleFileResource<CuratorConfig> {

    /**
     * @param file JSON file to parse
     */
    public CuratorJsonConfigResource(File file) {
        super(file);
    }
    
    /**
     * Perform the parsing of a JSON curator config file into a CuratorConfig object.
     *
     * An example of the expected syntax of this file is in 
     * src/test/resources/dummy-search_home/conf/config-repository/curator-config-test.json
     */
    @Override
    public CuratorConfig parse() throws IOException {
        log.debug("Reading curator configuration data from '" + file.getAbsolutePath() + "'");
        CuratorConfig result;
        
        try (InputStream is = new FileInputStream(file)) {
            result = new CuratorConfigParser().parseJsonCuratorConfiguration(is);
        }
        
        return result;
    }
}
