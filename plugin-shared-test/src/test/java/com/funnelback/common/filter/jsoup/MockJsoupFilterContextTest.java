package com.funnelback.common.filter.jsoup;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.hamcrest.core.StringContains;

public class MockJsoupFilterContextTest {

    @Test
    public void testPassingInHtmlDocument() {
        MockJsoupFilterContext filterContext = new MockJsoupFilterContext("<html>\n" + 
            "<body>\n" + 
            "<p>hello</p>\n" + 
            "</body>\n" + 
            "</html>");
        
        MatcherAssert.assertThat(filterContext.getDocument().text(), StringContains.containsString("hello"));
    }
    
    @Test
    public void testSettingConfigSettings() {
        MockJsoupFilterContext filterContext = new MockJsoupFilterContext("hi");
        filterContext.getSetup().setConfigSetting("a", "b");
        
        Assertions.assertEquals("b", filterContext.getSetup().getConfigSetting("a"));
    }
}