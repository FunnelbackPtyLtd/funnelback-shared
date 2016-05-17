package com.funnelback.publicui.test.search.web.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.web.controllers.ResourcesController;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class ResourcesControllerTest {

    private ResourcesController controller;
    
    @Autowired
    private MockConfigRepository configRepository;

    private MockHttpServletRequest req;
    
    private ResourceHttpRequestHandler resourceHandler;
    
    @Before
    public void before() {
        req = new MockHttpServletRequest();
        req.setMethod("GET");
        
        configRepository.removeAllCollections();
        Collection c = new Collection("resources-controller", new NoOptionsConfig("resources-controller"));
        c.getProfiles().put("_default", null);
        c.getProfiles().put("profile-folder", null);
        configRepository.addCollection(c);
        
        resourceHandler = Mockito.mock(ResourceHttpRequestHandler.class);
        
        controller = new ResourcesController() {
            @Override
            protected ResourceHttpRequestHandler getResourceHttpRequestHandler() {
                return resourceHandler;
            } 
        };

        controller.setConfigRepository(configRepository);
        controller.setContextPath("");
        controller.setCollectionWebResourcesDirectoryName("web");
        
    }
    
    @Test
    public void testInvalidCollection() throws Exception {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        req.setRequestURI("/resources/invalid-collection/file.txt");
        controller.handleRequest("invalid-collection", req, resp);        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
        
        req.setRequestURI("/resources/");
        resp = new MockHttpServletResponse();
        controller.handleRequest("", req, resp);        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());

        req.setRequestURI("/resources/file.txt");
        resp = new MockHttpServletResponse();
        controller.handleRequest("file.txt", req, resp);        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
    }
    
    @Test
    public void testInvalidProfileFallsBackToDefault() throws Exception {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        resp.setStatus(-1);
        
        List<Resource> locations = new ArrayList<Resource>();
        locations.add(new FileSystemResource(
                new File(configRepository.getCollection("resources-controller").getConfiguration().getConfigDirectory()
                        + File.separator + "_default"
                        + File.separator + "web")));
        
        req.setRequestURI("/resources/resources-controller/invalid-profile/file1.txt");
        controller.handleRequest("resources-controller", req, resp);
        
        Mockito.verify(resourceHandler).setLocations(Mockito.refEq(locations));
        Assert.assertEquals(req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE), "invalid-profile/file1.txt");
        Assert.assertEquals("Response code should be left unchanged", -1, resp.getStatus());
    }
       
    /**
     * Relies on Spring throwing an Exception because the test is not running
     * in a WebApplicationContext but in a TestContext.
     * @see https://jira.springsource.org/browse/SPR-5243
     */
    @Test
    public void testFiles() throws Exception {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        for (String[] path: new String[][] {
            {"/resources/resources-controller/file1.txt", "_default", "file1.txt"},
            {"/resources/resources-controller/_default/file1.txt", "_default", "file1.txt"},
            {"/resources/resources-controller/profile-folder/file2.txt", "profile-folder", "file2.txt"},
            {"/resources/resources-controller/file%20with%20spaces.txt", "_default", "file with spaces.txt"},
            {"/resources/resources-controller/sub-folder/sub-file1.txt", "_default", "sub-folder/sub-file1.txt"},
            {"/resources/resources-controller/_default/sub-folder/sub-file1.txt", "_default", "sub-folder/sub-file1.txt"},
            {"/resources/resources-controller/profile-folder/sub-folder/sub-file2.txt", "profile-folder", "sub-folder/sub-file2.txt"}
        }) {        
            Mockito.reset(resourceHandler);
            
            req.setRequestURI(path[0]);
            resp = new MockHttpServletResponse();
            controller.handleRequest("resources-controller", req, resp);
            
            List<Resource> locations = new ArrayList<Resource>();
            locations.add(new FileSystemResource(
                    new File(configRepository.getCollection("resources-controller").getConfiguration().getConfigDirectory()
                            + File.separator + path[1]
                            + File.separator + "web")));

            Mockito.verify(resourceHandler).setLocations(Mockito.refEq(locations));
            Assert.assertEquals(path[2], req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
        }
    }

    @Test
    public void testInvalidPath() throws Exception {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        req.setRequestURI("/resources/resources-controller/_default/../collection.cfg");
        controller.handleRequest("resources-controller", req, resp);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
    }
    
    @Test
    public void testInvalidRootPath() throws Exception {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        req.setRequestURI("/resources/resources-controller/_default//etc/passwd");
        controller.handleRequest("resources-controller", req, resp);        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
    }

}
