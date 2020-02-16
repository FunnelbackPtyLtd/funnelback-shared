package com.funnelback.publicui.test.search.model.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.trigger.AndTrigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class AndTriggerTests {

    @Test
    public void testAndTrigger() {
        AndTrigger at = new AndTrigger();
        
        SearchTransaction st = new SearchTransaction(null, null);
        
        Assert.assertTrue("Expected to activate with an empty trigger set", at.activatesOn(st)); 
        
        at.getTriggers().add(new NeverTrigger());
        
        Assert.assertFalse("Expected not to activate with one false sub-trigger", at.activatesOn(st)); 

        at.getTriggers().add(new AlwaysTrigger());
        
        Assert.assertFalse("Expected not to activate with one false sub-trigger (even if there's also a true one)", at.activatesOn(st));
        
        at.getTriggers().clear();
        
        at.getTriggers().add(new AlwaysTrigger());

        Assert.assertTrue("Expected to activate with one true trigger set", at.activatesOn(st)); 

        at.getTriggers().add(new NeverTrigger());

        Assert.assertFalse("Expected not to activate with one false sub-trigger (even if a true one is first)", at.activatesOn(st)); 
    }
    
//  Needs to move to a CuratorYamlConfigResourceTest
//    @Test
//    public void testSerializeAndTrigger() {
//        AndTrigger at = new AndTrigger();
//        at.getTriggers().add(new NeverTrigger());
//        at.getTriggers().add(new AllQueryWordsTrigger(Arrays.asList(new String[]{"uniqueword1"})));
//        at.getTriggers().add(new AllQueryWordsTrigger(Arrays.asList(new String[]{"uniqueword2"})));
//        at.getTriggers().add(new AlwaysTrigger());
//
//        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(at);
//        Assert.assertTrue("", yaml.contains("uniqueword1"));
//        Assert.assertTrue("", yaml.contains("uniqueword2"));
//    }
}
