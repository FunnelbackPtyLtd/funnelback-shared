package com.funnelback.common.filter.jsoup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MockJsoupFilterContextTest {

    @Test
    public void testPassingInHtmlDocument() {
        MockJsoupFilterContext filterContext = new MockJsoupFilterContext("<html>\n" + 
            "<body>\n" + 
            "<p>hello</p>\n" + 
            "</body>\n" + 
            "</html>");

        Assertions.assertTrue(filterContext.getDocument().text().contains("hello"));
    }
    
    @Test
    public void testSettingConfigSettings() {
        MockJsoupFilterContext filterContext = new MockJsoupFilterContext("hi");
        filterContext.getSetup().setConfigSetting("a", "b");
        
        Assertions.assertEquals("b", filterContext.getSetup().getConfigSetting("a"));
    }
}