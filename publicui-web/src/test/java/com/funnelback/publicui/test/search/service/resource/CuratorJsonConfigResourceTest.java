package com.funnelback.publicui.test.search.service.resource;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.service.resource.impl.CuratorJsonConfigResource;

public class CuratorJsonConfigResourceTest {
    
    @Test
    public void test() throws IOException {
        File f = new File("src/test/resources/dummy-search_home/conf/config-repository/curator-config-test.json");
        CuratorJsonConfigResource c = new CuratorJsonConfigResource(f);
        
        CuratorConfig conf = c.parse();
        
        Assert.assertEquals("Expected curator-config-test.json to contain two trigger actions.", 2, conf.getTriggerActions().size());
    }
}
