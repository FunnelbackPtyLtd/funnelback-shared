package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

/**
 * Parses a curator JSON config file and returns a {@link CuratorConfig} object.
 *
 * @see CuratorYamlConfigResource
 */
@Log4j
public class CuratorJsonConfigResource extends AbstractSingleFileResource<CuratorConfig> {

    /**
     * <p>Jackson mapper to convert to/from JSON</p>
     * 
     * <p>Type information must be embedded in the JSON because
     * we need to know which implementation of {@link Trigger} and {@link Action}
     * to deserialize</p>
     */
    private final ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();
    
    /**
     * @param file JSON file to parse
     */
    public CuratorJsonConfigResource(File file) {
        super(file);
        
        // 
        mapper.enableDefaultTyping();
    }

    @Override
    public CuratorConfig parse() throws IOException {
        log.debug("Reading curator configuration data from '" + file.getAbsolutePath() + "'");
        return mapper.readValue(this.file, CuratorConfig.class);
    }

    /**
     * Serializes a {@link CuratorConfig} into a JSON string
     * @param config {@link CuratorConfig} to serialize
     * @return JSON-serialized version
     * @throws IOException
     */
    public static String serialize(CuratorConfig config) throws IOException {
        return new ObjectMapper().enableDefaultTyping().writeValueAsString(config);
    }
    
    /**
     * Converts a YAML curator config into its JSON equivalent
     * @param yaml Curator config in YAML format
     * @return Curator config in JSON format
     * @throws IOException
     */
    public static String jsonFromYaml(String yaml) throws IOException {
        return serialize(CuratorYamlConfigResource.getYamlObject().loadAs(yaml, CuratorConfig.class));
    }
    
    /**
     * Small utility to convert a Curator YAML file into a JSON file
     * @param args First argument: Path to the YAML file.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: " + CuratorJsonConfigResource.class.getName() + " </path/to/config.yaml>");
            System.exit(-1);
        }
        
        System.out.println(jsonFromYaml(FileUtils.readFileToString(new File(args[0]))));
    }

}
