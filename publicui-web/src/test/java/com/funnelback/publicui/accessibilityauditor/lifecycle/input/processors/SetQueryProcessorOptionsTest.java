package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.wcag.model.WCAG20Technique;

public class SetQueryProcessorOptionsTest {

    @Test
    public void getMBLOptionTest() {
        String res = new SetQueryProcessorOptions().getMBLOption();
        
        Assert.assertTrue("MBL should be as big as the default MBL was: " + res,
            Integer.parseInt(res.split("=")[1]) >= 250);
        
        // 1 for the letter in the technique e.g. H
        // 1 for the number
        // 1 for the separator
        Assert.assertTrue("MBL should be more than 3 times the number of techniques MBL was: " + res,
            Integer.parseInt(res.split("=")[1]) >= WCAG20Technique.values().length * 3);
    }
}
