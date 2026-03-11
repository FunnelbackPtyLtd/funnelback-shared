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
    
    @Test
    public void testAddingNullKey() {
        PriorityQueryProcessorOptions priorityQPOs = new PriorityQueryProcessorOptions();
        Assertions.assertThrows(NullPointerException.class, () -> {
            priorityQPOs.addOption(null, "value");
        });
    }
    
    @Test
    public void testAddingNullValue() {
        PriorityQueryProcessorOptions priorityQPOs = new PriorityQueryProcessorOptions();
        Assertions.assertThrows(NullPointerException.class, () -> {
            priorityQPOs.addOption("key", null);
        });
    }
    
    @Test
    public void testEmptyOptions() {
        PriorityQueryProcessorOptions priorityQPOs = new PriorityQueryProcessorOptions();
        Assertions.assertNotNull(priorityQPOs.getOptions());
        Assertions.assertTrue(priorityQPOs.getOptions().isEmpty());
    }
    
    @Test
    public void testMultipleOptions() {
        PriorityQueryProcessorOptions priorityQPOs = new PriorityQueryProcessorOptions();
        priorityQPOs.addOption("key1", "value1");
        priorityQPOs.addOption("key2", "value2");
        priorityQPOs.addOption("key3", "value3");
        Assertions.assertEquals(3, priorityQPOs.getOptions().size());
        Assertions.assertEquals("value1", priorityQPOs.getOptions().get("key1"));
        Assertions.assertEquals("value2", priorityQPOs.getOptions().get("key2"));
        Assertions.assertEquals("value3", priorityQPOs.getOptions().get("key3"));
    }
}