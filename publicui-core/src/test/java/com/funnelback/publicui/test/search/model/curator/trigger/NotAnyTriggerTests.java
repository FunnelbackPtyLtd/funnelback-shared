package com.funnelback.publicui.test.search.model.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.trigger.NotAnyTrigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class NotAnyTriggerTests {

    @Test
    public void testAndTrigger() {
        NotAnyTrigger nat = new NotAnyTrigger();
        
        SearchTransaction st = new SearchTransaction(null, null);
        
        Assert.assertTrue("Expected to activate with an empty trigger set", nat.activatesOn(st)); 
        
        nat.getTriggers().add(new NeverTrigger());
        
        Assert.assertTrue("Expected to activate with one false sub-trigger", nat.activatesOn(st)); 

        nat.getTriggers().add(new AlwaysTrigger());
        
        Assert.assertFalse("Expected not to activate with one true sub-trigger (even if there's also a false one)", nat.activatesOn(st));
        
        nat.getTriggers().clear();
        
        nat.getTriggers().add(new AlwaysTrigger());

        Assert.assertFalse("Expected not to activate with one true trigger set", nat.activatesOn(st)); 

        nat.getTriggers().add(new NeverTrigger());

        Assert.assertFalse("Expected not to activate with one true sub-trigger (even if there's also a false one)", nat.activatesOn(st)); 
    }
    
//  Needs to move to a CuratorYamlConfigResourceTest
//    @Test
//    public void testSerializeNotAnyTrigger() {
//        NotAnyTrigger nat = new NotAnyTrigger();
//        nat.getTriggers().add(new NeverTrigger());
//        nat.getTriggers().add(new AllQueryWordsTrigger(Arrays.asList(new String[]{"uniqueword1"})));
//        nat.getTriggers().add(new AllQueryWordsTrigger(Arrays.asList(new String[]{"uniqueword2"})));
//        nat.getTriggers().add(new AlwaysTrigger());
//
//        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(nat);
//        Assert.assertTrue("", yaml.contains("uniqueword1"));
//        Assert.assertTrue("", yaml.contains("uniqueword2"));
//    }
}