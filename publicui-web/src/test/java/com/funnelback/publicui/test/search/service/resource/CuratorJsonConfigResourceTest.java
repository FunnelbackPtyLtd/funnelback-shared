package com.funnelback.publicui.test.search.service.resource;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.funnelback.publicui.curator.SimpleGroovyActionResourceManager;
import com.funnelback.publicui.curator.SimpleGroovyTriggerResourceManager;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.service.resource.impl.CuratorJsonConfigResource;

public class CuratorJsonConfigResourceTest {
    
    @Test
    public void test() throws IOException {
        File f = File.createTempFile(this.getClass().getName(), ".tmp");
        f.deleteOnExit();
        
        FileUtils.writeStringToFile(f, 
            "[\n"+
            "   {\n"+
            "      \"name\":\"Some name\",\n"+
            "      \"trigger\":{\n"+
            "         \"type\":\"AllQueryWords\",\n"+
            "         \"triggerWords\":[\n"+
            "            \"best\",\n"+
            "            \"king\"\n"+
            "         ]\n"+
            "      },\n"+
            "      \"actions\":[\n"+
            "         {\n"+
            "            \"type\":\"DisplayMessage\",\n"+
            "            \"message\":{\n"+
            "               \"messageHtml\":\"json-message1html\",\n"+
            "               \"additionalProperties\":null,\n"+
            "               \"category\":\"no-category\"\n"+
            "            }\n"+
            "         }\n"+
            "      ]\n"+
            "   }\n"+
            "]\n"+
            ""
        );
        
        CuratorJsonConfigResource c = new CuratorJsonConfigResource(f, new SimpleGroovyTriggerResourceManager(), new SimpleGroovyActionResourceManager());
        
        CuratorConfig conf = c.parse();
        
        System.out.println(conf.toString());
    }
}
