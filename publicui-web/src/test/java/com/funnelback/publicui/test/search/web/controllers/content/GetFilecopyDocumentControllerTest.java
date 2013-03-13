package com.funnelback.publicui.test.search.web.controllers.content;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs.FileSystemException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.web.controllers.content.GetFilecopyDocumentController;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class GetFilecopyDocumentControllerTest {

    private static final File TEST_FILE = new File("src/test/resources/dummy-search_home/conf/filecopy/collection.cfg");
    
    @Autowired
    private GetFilecopyDocumentController controller;
    
    @Autowired
    private MockConfigRepository configRepository;

    private URI uri;
    private URI invalidUri;

    @Before
    public void before() throws Exception {
        configRepository.addCollection(
            new Collection("filecopy",
                new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "filecopy")
                    .setValue("collection_type", "filecopy")
                    .setValue(Keys.FileCopy.USERNAME, "")
                    .setValue(Keys.FileCopy.PASSWORD, "")
                    .setValue(Keys.FileCopy.DOMAIN, "")));
        configRepository.addCollection(
            new Collection("dummy",
                new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "dummy")
                    .setValue("collection_type", "web")));
        
        if (OS.isFamilyWindows()) {
            uri = new URI("file:///"+TEST_FILE.getAbsolutePath().replace("\\", "/"));
            invalidUri = new URI("file:///C:/non-existent.ext");
        } else {
            uri = new URI("file://"+TEST_FILE.getAbsolutePath());
            invalidUri = new URI("file:///non-existent/file.ext");
        }
    }
    
    @Test
    public void testInvalidCollection() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("non-existent", null, false, response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
    
    @Test
    public void testWrongCollectionType() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("dummy", invalidUri, false, response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    @Test(expected=FileSystemException.class)
    public void testNonExistentUriNoDls() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", invalidUri, false, response, new MockHttpServletRequest());
    }
    
    @Test
    public void testNoDls() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", uri, false, response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(TEST_FILE),
            response.getContentAsByteArray());
        Assert.assertEquals("application/octet-stream", response.getContentType());
        Assert.assertEquals(
            "attachment; filename=\"collection.cfg\"",
            response.getHeaderValue("Content-Disposition"));

    }

}
