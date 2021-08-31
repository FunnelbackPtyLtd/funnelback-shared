package com.funnelback.plugin.search.mock;

import com.funnelback.plugin.search.model.SuggestionQuery;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MockSearchLifeCycleContextTest {

    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockSearchLifeCycleContext ctx = new MockSearchLifeCycleContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        Assert.assertTrue(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }

    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockSearchLifeCycleContext ctx = new MockSearchLifeCycleContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        Assert.assertTrue(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
        Assert.assertTrue(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assert.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }

    @Test
    public void readMissingPluginConfigurationFile() {
        MockSearchLifeCycleContext ctx = new MockSearchLifeCycleContext();
        Assert.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assert.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }

    @Test
    public void getSuggestionQueryResultTest() {
        MockSearchLifeCycleContext ctx = new MockSearchLifeCycleContext();
        SuggestionQuery suggestionQuery = new SuggestionQuery("te*");
        ctx.setMockSuggestions(suggestionQuery, List.of("test", "tester", "tea"));
        Assert.assertEquals(3, ctx.getSuggestionQueryResult(suggestionQuery).size());
    }
}
