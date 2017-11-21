package com.funnelback.publicui.utils.web;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.padre.NumRanks;

public class NumRanksLimitCheckTest {

    private final NumRanksLimitCheck limitCheck = new NumRanksLimitCheck();
    
    @Test
    public void testNumRanksLimit() {
        Assert.assertFalse(limitCheck.numRanksExceeded("", new NumRanks(1000)));
        
        Assert.assertFalse(limitCheck.numRanksExceeded("a12345678900000", new NumRanks(1000)));
        Assert.assertFalse(limitCheck.numRanksExceeded("12345678900000.0", new NumRanks(1000)));
        
        Assert.assertFalse(limitCheck.numRanksExceeded("0", new NumRanks(1000)));
        Assert.assertFalse(limitCheck.numRanksExceeded("1000", new NumRanks(1000)));
        
        Assert.assertTrue(limitCheck.numRanksExceeded("1001", new NumRanks(1000)));
        Assert.assertTrue(limitCheck.numRanksExceeded("2000000000", new NumRanks(1000)));
        Assert.assertTrue(limitCheck.numRanksExceeded("9000000000", new NumRanks(1000)));
        Assert.assertTrue(limitCheck.numRanksExceeded("900000000000000000000000000000000000000000000000000000000000000000", new NumRanks(1000)));
    }
}
