package com.funnelback.publicui.search.model.transaction;

import org.junit.Assert;
import org.junit.Test;

public class PriorityQueryProcessorOptionsTest {

    @Test
    public void testAddingTheSameOption() {
        PriorityQueryProcessorOptions priorityQPOs = new PriorityQueryProcessorOptions();
        priorityQPOs.addOption("key", "first");
        priorityQPOs.addOption("key", "second");
        Assert.assertEquals("first", priorityQPOs.getOptions().get("key"));
    }
}
