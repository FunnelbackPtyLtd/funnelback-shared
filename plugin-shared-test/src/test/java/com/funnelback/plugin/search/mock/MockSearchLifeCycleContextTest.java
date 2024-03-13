package com.funnelback.plugin.search.mock;

import com.funnelback.plugin.search.model.SuggestionQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MockSearchLifeCycleContextTest {

    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockSearchLifeCycleContext ctx = new MockSearchLifeCycleContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        Assertions.assertTrue(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }

    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockSearchLifeCycleContext ctx = new MockSearchLifeCycleContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        Assertions.assertTrue(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
        Assertions.assertTrue(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assertions.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }

    @Test
    public void readMissingPluginConfigurationFile() {
        MockSearchLifeCycleContext ctx = new MockSearchLifeCycleContext();
        Assertions.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assertions.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }

    @Test
    public void getSuggestionQueryResultTest() {
        MockSearchLifeCycleContext ctx = new MockSearchLifeCycleContext();
        SuggestionQuery suggestionQuery = new SuggestionQuery("te*");
        ctx.setMockSuggestions(suggestionQuery, List.of("test", "tester", "tea"));
        Assertions.assertEquals(3, ctx.getSuggestionQueryResult(suggestionQuery).size());
    }
}