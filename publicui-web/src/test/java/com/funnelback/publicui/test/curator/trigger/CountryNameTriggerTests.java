package com.funnelback.publicui.test.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.trigger.CountryNameTrigger;
import com.funnelback.publicui.search.model.geolocation.Location;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorYamlConfigResource;

public class CountryNameTriggerTests {

    @Test
    public void testCountryNameTrigger() {
        CountryNameTrigger cnt = new CountryNameTrigger();
        
        SearchQuestion question = new SearchQuestion();
        com.maxmind.geoip.Location maxmind = new com.maxmind.geoip.Location();
        maxmind.countryName = "Australia";
        question.setLocation(new Location(maxmind));
        SearchTransaction st = new SearchTransaction(question, null);
        
        Assert.assertFalse("Expected not to activate with an empty country set", cnt.activatesOn(st, null)); 

        cnt.getTargetCounties().add("China");
        
        Assert.assertFalse("Expected not to activate when countries don't match", cnt.activatesOn(st, null)); 

        cnt.getTargetCounties().add("australia");
        
        Assert.assertFalse("Expected not to activate when countries don't match in case", cnt.activatesOn(st, null)); 

        cnt.getTargetCounties().add("Aust");
        
        Assert.assertFalse("Expected not to activate when countries don't match (even prefixes)", cnt.activatesOn(st, null)); 

        cnt.getTargetCounties().add("Australia");
        
        Assert.assertTrue("Expected to activate when countries do match", cnt.activatesOn(st, null)); 
    }
    
    @Test
    public void testSerializeAndTrigger() {
        CountryNameTrigger cnt = new CountryNameTrigger();
        cnt.getTargetCounties().add("uniquecountry");

        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(cnt);
        Assert.assertTrue("", yaml.contains("uniquecountry"));
    }
}
