package com.funnelback.publicui.test.search.model.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.trigger.CountryNameTrigger;
import com.funnelback.publicui.search.model.geolocation.Location;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class CountryNameTriggerTests {

    @Test
    public void testCountryNameTrigger() {
        CountryNameTrigger cnt = new CountryNameTrigger();
        
        SearchQuestion question = new SearchQuestion();
        Location location = Location.builder()
                                        .countryName("Australia")
                                        .build();
        
        question.setLocation(location);
        SearchTransaction st = new SearchTransaction(question, null);
        
        Assert.assertFalse("Expected not to activate with an empty country set", cnt.activatesOn(st)); 

        cnt.getTargetCountries().add("China");
        
        Assert.assertFalse("Expected not to activate when countries don't match", cnt.activatesOn(st)); 

        cnt.getTargetCountries().add("australia");
        
        Assert.assertFalse("Expected not to activate when countries don't match in case", cnt.activatesOn(st)); 

        cnt.getTargetCountries().add("Aust");
        
        Assert.assertFalse("Expected not to activate when countries don't match (even prefixes)", cnt.activatesOn(st)); 

        cnt.getTargetCountries().add("Australia");
        
        Assert.assertTrue("Expected to activate when countries do match", cnt.activatesOn(st)); 
    }
    
//  Needs to move to a CuratorYamlConfigResourceTest
//    @Test
//    public void testSerializeAndTrigger() {
//        CountryNameTrigger cnt = new CountryNameTrigger();
//        cnt.getTargetCountries().add("uniquecountry");
//
//        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(cnt);
//        Assert.assertTrue("", yaml.contains("uniquecountry"));
//    }
}
