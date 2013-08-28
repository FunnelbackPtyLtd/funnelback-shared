package com.funnelback.curator.trigger;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource;

public class OrTriggerTests {

    @Test
    public void testOrTrigger() {
        OrTrigger at = new OrTrigger();
        
        SearchTransaction st = new SearchTransaction(null, null);
        
        Assert.assertFalse("Expected not to activate with an empty trigger set", at.activatesOn(st)); 
        
        at.getTriggers().add(new NeverTrigger());
        
        Assert.assertFalse("Expected not to activate with one false sub-trigger", at.activatesOn(st)); 

        at.getTriggers().add(new AlwaysTrigger());
        
        Assert.assertTrue("Expected to activate with one true sub-trigger (even if there's also a false one)", at.activatesOn(st));
        
        at.getTriggers().clear();
        
        at.getTriggers().add(new AlwaysTrigger());

        Assert.assertTrue("Expected to activate with one true trigger set", at.activatesOn(st)); 

        at.getTriggers().add(new NeverTrigger());

        Assert.assertTrue("Expected to activate with one true sub-trigger (followed by a false one)", at.activatesOn(st)); 
    }
    
    @Test
    public void testSerializeOrTrigger() {
        OrTrigger ot = new OrTrigger();
        ot.getTriggers().add(new NeverTrigger());
        ot.getTriggers().add(new AllQueryWordsTrigger(Arrays.asList(new String[]{"uniqueword1"})));
        ot.getTriggers().add(new AllQueryWordsTrigger(Arrays.asList(new String[]{"uniqueword2"})));
        ot.getTriggers().add(new AlwaysTrigger());

        String yaml = CuratorConifgResource.getYamlObject().dumpAsMap(ot);
        Assert.assertTrue("", yaml.contains("uniqueword1"));
        Assert.assertTrue("", yaml.contains("uniqueword2"));
    }
}
