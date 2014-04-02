package com.funnelback.publicui.test.curator.trigger;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.trigger.DateRangeTrigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConfigResource;

public class DateRangeTriggerTests {

    @Test
    public void testDateRangeTrigger() {
        DateRangeTrigger drt = new DateRangeTrigger();

        Calendar c = Calendar.getInstance();
        
        SearchTransaction st = new SearchTransaction(null, null);

        Assert.assertTrue("Expected to activate when neither bound is set", drt.activatesOn(st, null)); 

        c.set(1899, 11, 31, 23, 59, 59);
        drt.setStartDate(c.getTime());

        Assert.assertTrue("Expected to activate when now is after start date (and end is null)", drt.activatesOn(st, null)); 

        c.set(2299, 11, 31, 23, 59, 59);
        drt.setEndDate(c.getTime());

        Assert.assertTrue("Expected to activate when now is after start date and before end date", drt.activatesOn(st, null)); 

        c.set(2298, 11, 31, 23, 59, 59);
        drt.setStartDate(c.getTime());

        Assert.assertFalse("Expected not to activate when now is after start date is in the future", drt.activatesOn(st, null)); 

        drt.setEndDate(null);

        Assert.assertFalse("Expected not to activate when now is after start date is in the future (and end is null)", drt.activatesOn(st, null)); 

        c.set(1899, 11, 31, 23, 59, 59);
        drt.setStartDate(c.getTime());

        c.set(1901, 11, 31, 23, 59, 59);
        drt.setEndDate(c.getTime());

        Assert.assertFalse("Expected not to activate when now is after end date", drt.activatesOn(st, null)); 

        drt.setStartDate(null);

        Assert.assertFalse("Expected not to activate when now is after end date (and start date is null)", drt.activatesOn(st, null)); 
    }
    
    @Test
    public void testSerializeDateRangeTrigger() {
        DateRangeTrigger drt = new DateRangeTrigger();

        Calendar c = Calendar.getInstance();
        
        c.set(1899, 11, 31, 23, 59, 59);
        drt.setStartDate(c.getTime());

        c.set(2298, 11, 31, 23, 59, 59);
        drt.setEndDate(c.getTime());
        
        String yaml = CuratorConfigResource.getYamlObject().dumpAsMap(drt);
        Assert.assertTrue("", yaml.contains("1899"));
        Assert.assertTrue("", yaml.contains("2298"));
    }
}
