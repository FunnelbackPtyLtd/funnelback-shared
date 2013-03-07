package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.search.web.views.freemarker.CustomisableFreeMarkerFormView;

public class CustomisableFreeMarkerSearchFormViewTest extends CustomisableFreeMarkerFormView {

    private Map<String, Object> model;
    private Config config;
    private MockHttpServletResponse response;
    
    @Before
    public void before() throws Exception {
        model = new HashMap<String, Object>();
        
        config = new NoOptionsConfig("dummy");
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(new Collection("dummy", config));
        
        model.put(SearchController.ModelAttributes.question.toString(), sq);
        
        response = new MockHttpServletResponse();
        response.setContentType("text/html");
    }
    
    @Test
    public void testNothingToDo() {
        // No custom content type or header
        customiseOutput("conf/dummy/_default/simple.ftl", model, response);
        
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals(1, response.getHeaderNames().size());
        Assert.assertEquals("Content-Type", response.getHeaderNames().iterator().next());
    }
    
    @Test
    public void testCustomContentType() {
        config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX, "test/junit");
        customiseOutput("conf/dummy/_default/simple.ftl", model, response);

        Assert.assertEquals("test/junit", response.getContentType());
        Assert.assertEquals(1, response.getHeaderNames().size());
        Assert.assertEquals("Content-Type", response.getHeaderNames().iterator().next());
    }
    
    @Test
    public void testCustomHeaders() {
        config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_COUNT_SUFFIX, "2");
        config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_SUFFIX + ".1", "First-Header: Value 1   ");
        config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_SUFFIX + ".2", "Second-Header     :second value...");
        // The third one should be ignored
        config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_SUFFIX + ".3", "Third-Header: value 3");
        
        customiseOutput("conf/dummy/_default/simple.ftl", model, response);

        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals(3, response.getHeaderNames().size());
        Assert.assertEquals("text/html", response.getHeader("Content-Type"));
        Assert.assertEquals("Value 1", response.getHeader("First-Header"));
        Assert.assertEquals("second value...", response.getHeader("Second-Header"));
    }

    @Test
    public void testBoth() {
        config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX, "text/csv");
        config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_COUNT_SUFFIX, "1");
        config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_SUFFIX + ".1", "Content-Disposition: attachment");

        customiseOutput("conf/dummy/_default/simple.ftl", model, response);

        Assert.assertEquals("text/csv", response.getContentType());
        Assert.assertEquals(2, response.getHeaderNames().size());
        Assert.assertEquals("text/csv", response.getHeader("Content-Type"));
        Assert.assertEquals("attachment", response.getHeader("Content-Disposition"));
        
    }
    
}
