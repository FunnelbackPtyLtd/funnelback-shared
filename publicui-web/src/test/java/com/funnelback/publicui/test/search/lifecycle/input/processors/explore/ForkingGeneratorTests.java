package com.funnelback.publicui.test.search.lifecycle.input.processors.explore;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.exec.OS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.processors.explore.ForkingGenerator;
import com.funnelback.publicui.search.model.collection.Collection;

public class ForkingGeneratorTests {
    
    private ForkingGenerator generator;
    
    @Before
    public void before() {
        generator = new ForkingGenerator();
        generator.setSearchHome(new File("src/test/resources/dummy-search_home"));
        
        String ext = ".sh";
        if (OS.isFamilyWindows()) {
            ext = ".bat";
        }
        generator.setPadreRfBinary("padre-rf" + ext);
    }
    
    @Test
    public void test() throws FileNotFoundException, EnvironmentVariableException {
        String q = generator.getExploreQuery(new Collection("dummy", new NoOptionsConfig("dummy")), "http://dummy.com/", 10);
        
        Assert.assertEquals("heath^1.6158 camp^1.6158 entire^1.2664 palace^1.2084 french^1.1012 near^0.8637 tent^0.8011 play^0.7935 british^0.6693 wood^0.6365", q);
    }

    @Test
    public void testBadPadreRfBinary() throws Exception {
        generator.setPadreRfBinary("Bad one");
        
        String q = generator.getExploreQuery(new Collection("dummy", new NoOptionsConfig("dummy")), "http://dummy.com/", null);
        Assert.assertNull(q);
    }
    
    public void testBadPadreRfOutput() throws Exception {
        String ext = ".sh";
        if (OS.isFamilyWindows()) {
            ext = ".bat";
        }
        generator.setPadreRfBinary("padre-rf-bad" + ext);
        
        String q = generator.getExploreQuery(new Collection("dummy", new NoOptionsConfig("dummy")), "http://dummy.com/", 10);
        Assert.assertNull(q);
        
    }
}
