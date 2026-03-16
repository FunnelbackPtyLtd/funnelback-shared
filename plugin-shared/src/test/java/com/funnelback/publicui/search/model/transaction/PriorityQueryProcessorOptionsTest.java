package com.funnelback.publicui.search.model.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PriorityQueryProcessorOptionsTest {

    @Test
    public void testAddingTheSameOption() {
        PriorityQueryProcessorOptions priorityQPOs = new PriorityQueryProcessorOptions();
        priorityQPOs.addOption("key", "first");
        priorityQPOs.addOption("key", "second");
        Assertions.assertEquals("first", priorityQPOs.getOptions().get("key"));
    }
}