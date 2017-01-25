package com.funnelback.publicui.test.search.service.resource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.service.resource.impl.CuratorJsonConfigResource;

public class CuratorJsonConfigResourceTest {
    
    @Test
    public void test() throws IOException {
        File f = new File("src/test/resources/dummy-search_home/conf/config-repository/curator-config-test.json");
        CuratorJsonConfigResource c = new CuratorJsonConfigResource(f);
        
        CuratorConfig conf = Optional.ofNullable(c.parse()).map(r -> r.getResource()).orElse(null);
        
        Assert.assertEquals("Expected curator-config-test.json to contain two trigger actions.", 2, conf.getTriggerActions().size());
    }
}
