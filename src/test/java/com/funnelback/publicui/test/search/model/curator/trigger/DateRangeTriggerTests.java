package com.funnelback.publicui.test.search.model.curator.trigger;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.trigger.DateRangeTrigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class DateRangeTriggerTests {

    @Test
    public void testDateRangeTrigger() {
        DateRangeTrigger drt = new DateRangeTrigger();

        Calendar c = Calendar.getInstance();
        
        SearchTransaction st = new SearchTransaction(null, null);

        Assert.assertTrue("Expected to activate when neither bound is set", drt.activatesOn(st)); 

        c.set(1899, 11, 31, 23, 59, 59);
        drt.setStartDate(c.getTime());

        Assert.assertTrue("Expected to activate when now is after start date (and end is null)", drt.activatesOn(st)); 

        c.set(2299, 11, 31, 23, 59, 59);
        drt.setEndDate(c.getTime());

        Assert.assertTrue("Expected to activate when now is after start date and before end date", drt.activatesOn(st)); 

        c.set(2298, 11, 31, 23, 59, 59);
        drt.setStartDate(c.getTime());

        Assert.assertFalse("Expected not to activate when now is after start date is in the future", drt.activatesOn(st)); 

        drt.setEndDate(null);

        Assert.assertFalse("Expected not to activate when now is after start date is in the future (and end is null)", drt.activatesOn(st)); 

        c.set(1899, 11, 31, 23, 59, 59);
        drt.setStartDate(c.getTime());

        c.set(1901, 11, 31, 23, 59, 59);
        drt.setEndDate(c.getTime());

        Assert.assertFalse("Expected not to activate when now is after end date", drt.activatesOn(st)); 

        drt.setStartDate(null);

        Assert.assertFalse("Expected not to activate when now is after end date (and start date is null)", drt.activatesOn(st)); 
    }
    
//  Needs to move to a CuratorYamlConfigResourceTest
//    @Test
//    public void testSerializeDateRangeTrigger() {
//        DateRangeTrigger drt = new DateRangeTrigger();
//
//        Calendar c = Calendar.getInstance();
//        
//        c.set(1899, 11, 31, 23, 59, 59);
//        drt.setStartDate(c.getTime());
//
//        c.set(2298, 11, 31, 23, 59, 59);
//        drt.setEndDate(c.getTime());
//        
//        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(drt);
//        Assert.assertTrue("", yaml.contains("1899"));
//        Assert.assertTrue("", yaml.contains("2298"));
//    }
}
