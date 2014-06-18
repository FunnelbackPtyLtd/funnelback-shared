package com.funnelback.publicui.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.curator.config.TriggerActions;

/**
 * Parser for JSON curator config files.
 */
public class CuratorConfigParser {

    /**
     * Perform the parsing of a JSON curator config input stream into a CuratorConfig object.
     *
     * Information on the expected syntax of the config is at http://docs.funnelback.com/curator.html
     */
    public CuratorConfig parseJsonCuratorConfiguration(InputStream configurationStream) throws JsonParseException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createJsonParser(configurationStream);
        jp.setCodec(objectMapper);
        
        List<TriggerActions> triggerActions = new ArrayList<TriggerActions>();
        
        if (jp.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalArgumentException("Error - Curator Json config file expected to have a top-level array");
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
