package com.funnelback.publicui.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

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

    public String generateJsonCuratorConfiguration(CuratorConfig newConfig) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory f = new JsonFactory();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JsonGenerator jg = f.createJsonGenerator(baos);
            jg.setCodec(objectMapper);
            jg.setPrettyPrinter(new DefaultPrettyPrinter());
            
            jg.writeStartArray();
            for (TriggerActions ta : newConfig.getTriggerActions()) {
                jg.writeObject(ta);
            }
            jg.writeEndArray();
            jg.close();
            
            return baos.toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TriggerActions parseTriggerActionsJson(byte[] content) {
        TriggerActions newTriggerActions;
        
        String parseString = new String(content, Charset.forName("ISO-8859-1")); // Preserves bytes regardless of the original encoding
        parseString = "[\n" + parseString + "\n]\n"; // Wrap in a json array - i.e. make it a config with one entry
        byte[] parseBytes = parseString.getBytes(Charset.forName("ISO-8859-1"));
        try {
            CuratorConfig newConfig = this.parseJsonCuratorConfiguration(new ByteArrayInputStream(parseBytes));
            if (newConfig.getTriggerActions().size() != 1) {
                throw new IllegalArgumentException("Expected argument to contain a single TriggerActions definition.");
            }
            
            newTriggerActions = newConfig.getTriggerActions().get(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse curator JSON", e);
        }
        return newTriggerActions;
    }

    public String generateTriggerActionsJson(TriggerActions triggerActions) {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory f = new JsonFactory();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JsonGenerator jg = f.createJsonGenerator(baos);
            jg.setCodec(objectMapper);
            jg.setPrettyPrinter(new DefaultPrettyPrinter());

            jg.writeObject(triggerActions);

            jg.close();
            
            return baos.toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}