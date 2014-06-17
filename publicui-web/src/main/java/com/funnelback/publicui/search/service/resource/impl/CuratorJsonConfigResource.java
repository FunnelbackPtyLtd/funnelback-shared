package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.InjectableValues;
import org.codehaus.jackson.map.ObjectMapper;

import com.funnelback.publicui.curator.GroovyActionResourceManager;
import com.funnelback.publicui.curator.GroovyTriggerResourceManager;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.curator.config.TriggerActions;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

/**
 * Parses a curator JSON config file and returns a {@link CuratorConfig} object.
 *
 * @see CuratorYamlConfigResource
 */
@Log4j
public class CuratorJsonConfigResource extends AbstractSingleFileResource<CuratorConfig> {

    private GroovyTriggerResourceManager triggerResourceManager;
    private GroovyActionResourceManager actionResourceManager;

    /**
     * @param file JSON file to parse
     * @param triggerResourceManager A resource manager for getting groovy trigger implementations 
     * @param actionResourceManager A resource manager for getting groovy action implementations 
     */
    public CuratorJsonConfigResource(File file, GroovyTriggerResourceManager triggerResourceManager,
        GroovyActionResourceManager actionResourceManager) {
        super(file);
        this.triggerResourceManager = triggerResourceManager;
        this.actionResourceManager = actionResourceManager;
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
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setInjectableValues(
            new InjectableValues.Std()
            .addValue(GroovyTriggerResourceManager.class, triggerResourceManager)
            .addValue(GroovyActionResourceManager.class, actionResourceManager)
            );
        
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createJsonParser(this.file);
        jp.setCodec(objectMapper);
        
        List<TriggerActions> triggerActions = new ArrayList<TriggerActions>();
        
        if (jp.nextToken() != JsonToken.START_ARRAY) {
            throw new RuntimeException("Error - Curator Json config file expected to have a top-level array");
        }
        
        while (jp.nextToken() == JsonToken.START_OBJECT) {
            TriggerActions ta = jp.readValueAs(TriggerActions.class);
            triggerActions.add(ta);
        }
        
        CuratorConfig result =  new CuratorConfig();
        result.addAll(triggerActions);
        return result;
    }
}
