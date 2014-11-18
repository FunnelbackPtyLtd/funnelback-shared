package com.funnelback.publicui.test.search.web.controllers.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.web.controllers.content.GetTrimDocumentController;
import com.funnelback.publicui.search.web.controllers.content.GetTrimDocumentController.ModelAttributes;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class GetTrimDocumentControllerTest {

    @Autowired
    private GetTrimDocumentController controller;
    
    @Autowired
    private MockConfigRepository configRepository;
    
    @Autowired
    private File searchHome;
    
    @Before
    public void before() throws FileNotFoundException {
        configRepository.addCollection(new Collection("web", new NoOptionsConfig(searchHome, "dummy")
            .setValue("collection_type", "web")));
        
        configRepository.addCollection(new Collection("trim", new NoOptionsConfig(searchHome, "dummy")
            .setValue("collection_type", "trim")
            .setValue("trim.database", "AB")
            .setValue("trim.license_number", "1234")));
    }
    
    @Test
    public void testWrongCollectionType() {
        MockHttpServletResponse response = new MockHttpServletResponse();        
        controller.getTrimReference("web", 123, false, response);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    @Test
    public void testNoCollection() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getTrimReference("unknown", 123, false, response);
        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
    
    @Test
    public void test() throws UnsupportedEncodingException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = controller.getTrimReference("trim", 123, false, response);
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("application/octet-stream", response.getContentType());
        Assert.assertEquals(
            "attachment; filename=\"search-result-123.tr5\"",
            response.getHeaderValue("Content-Disposition"));
        
        Assert.assertEquals(
            mav.getModel().get(ModelAttributes.trimLicenseNumber.toString()),
            "1234");
        Assert.assertEquals(
            mav.getModel().get(ModelAttributes.trimDatabase.toString()),
            "AB");
        Assert.assertEquals(
            mav.getModel().get(ModelAttributes.uri.toString()),
            "123");
        
        response = new MockHttpServletResponse();
        mav = controller.getTrimReference("trim", 123, true, response);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("text/html", response.getContentType());
        
        Assert.assertEquals(
            mav.getModel().get(ModelAttributes.trimLicenseNumber.toString()),
            "1234");
        Assert.assertEquals(
            mav.getModel().get(ModelAttributes.trimDatabase.toString()),
            "AB");
        Assert.assertEquals(
            mav.getModel().get(ModelAttributes.uri.toString()),
            "123");

    }
    
    
}
