package com.funnelback.common.filter.jsoup;

import org.junit.Assert;
import org.junit.Test;

import org.hamcrest.core.StringContains;

public class MockJsoupFilterContextTest {

    @Test
    public void testPassingInHtmlDocument() {
        MockJsoupFilterContext filterContext = new MockJsoupFilterContext("<html>\n" + 
            "<body>\n" + 
            "<p>hello</p>\n" + 
            "</body>\n" + 
            "</html>");
        
        Assert.assertThat(filterContext.getDocument().text(), StringContains.containsString("hello"));
    }
    
    @Test
    public void testSettingConfigSettings() {
        MockJsoupFilterContext filterContext = new MockJsoupFilterContext("hi");
        filterContext.getSetup().getConfigSettings().put("a", "b");
        
        Assert.assertEquals("b", filterContext.getSetup().getConfigSetting("a"));
    }
}
