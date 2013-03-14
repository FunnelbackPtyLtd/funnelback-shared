package com.funnelback.publicui.test.search.web.controllers.content;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.vfs.FileSystemException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.DefaultValues;
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
    private URI bigDocUri;
    private URI invalidUri;

    @Before
    public void before() throws Exception {
        configRepository.removeAllCollections();
        
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
        configRepository.addCollection(
            new Collection("filecopy-dls",
                new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "filecopy")
                    .setValue("collection_type", "filecopy")
                    .setValue(Keys.FileCopy.SECURITY_MODEL, DefaultValues.FileCopy.SECURITY_MODEL_NTFS)
                    .setValue(Keys.ModernUI.AUTHENTICATION, "true")
                    .setValue(Keys.FileCopy.USERNAME, "")
                    .setValue(Keys.FileCopy.PASSWORD, "")
                    .setValue(Keys.FileCopy.DOMAIN, "")));
        
        if (OS.isFamilyWindows()) {
            uri = new URI("file:///"+TEST_FILE.getAbsolutePath().replace("\\", "/"));
            bigDocUri = new URI("file:///"
                + new File("src/test/resources/dummy-search_home/conf/filecopy/shakespeare.html").getAbsolutePath().replace("\\", "/"));
            invalidUri = new URI("file:///C:/non-existent.ext");
        } else {
            uri = new URI("file://"+TEST_FILE.getAbsolutePath());
            bigDocUri = new URI("file:///"
                + new File("src/test/resources/dummy-search_home/conf/filecopy/shakespeare.html").getAbsolutePath());
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
        Assert.assertEquals("false", response.getHeaderValue("X-Funnelback-DLS"));
        Assert.assertEquals("application/octet-stream", response.getContentType());
        Assert.assertEquals(
            "attachment; filename=\"collection.cfg\"",
            response.getHeaderValue("Content-Disposition"));
    }
    
    @Test
    public void testDlsEarlyBinding() throws Exception {
        if (OS.isFamilyWindows()) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setUserPrincipal(new MockPrincipal());
            
            controller.getFilecopyDocument("filecopy-dls", uri, false, response, request);
            
            Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            Assert.assertArrayEquals(
                FileUtils.readFileToByteArray(TEST_FILE),
                response.getContentAsByteArray());
            Assert.assertEquals("true", response.getHeaderValue("X-Funnelback-DLS"));
            Assert.assertEquals("application/octet-stream", response.getContentType());
            Assert.assertEquals(
                "attachment; filename=\"collection.cfg\"",
                response.getHeaderValue("Content-Disposition"));
        }
    }
    
    @Test
    public void testDlsLateBinding() throws Exception {
        if (OS.isFamilyWindows()) {
        
            configRepository.getCollection("filecopy-dls")
                .getConfiguration().setValue(Keys.FileCopy.SECURITY_MODEL, DefaultValues.FileCopy.SECURITY_MODEL_NONE)
                    .setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_ACTION, "ntfs")
                    .setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, "custom");
    
            
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setUserPrincipal(new MockPrincipal());
            
            controller.getFilecopyDocument("filecopy-dls", uri, false, response, request);
            
            Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            Assert.assertArrayEquals(
                FileUtils.readFileToByteArray(TEST_FILE),
                response.getContentAsByteArray());
            Assert.assertEquals("true", response.getHeaderValue("X-Funnelback-DLS"));
            Assert.assertEquals("application/octet-stream", response.getContentType());
            Assert.assertEquals(
                "attachment; filename=\"collection.cfg\"",
                response.getHeaderValue("Content-Disposition"));
        }
    }

    @Test
    public void testDlsLateBindingDisabled() throws Exception {
        if (OS.isFamilyWindows()) {
        
            configRepository.getCollection("filecopy-dls")
                .getConfiguration().setValue(Keys.FileCopy.SECURITY_MODEL, DefaultValues.FileCopy.SECURITY_MODEL_NONE)
                    .setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_ACTION, "ntfs")
                    .setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, "disabled");
    
            
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setUserPrincipal(new MockPrincipal());
            
            controller.getFilecopyDocument("filecopy-dls", uri, false, response, request);
            
            Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            Assert.assertArrayEquals(
                FileUtils.readFileToByteArray(TEST_FILE),
                response.getContentAsByteArray());
            Assert.assertEquals("false", response.getHeaderValue("X-Funnelback-DLS"));
            Assert.assertEquals("application/octet-stream", response.getContentType());
            Assert.assertEquals(
                "attachment; filename=\"collection.cfg\"",
                response.getHeaderValue("Content-Disposition"));
        }
    }
    
    @Test
    public void testDlsNoAuthConfigured() throws Exception {
        configRepository.getCollection("filecopy-dls")
            .getConfiguration().setValue(Keys.ModernUI.AUTHENTICATION, "false");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new MockPrincipal());
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy-dls", uri, false, response, request);
        
        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    
    @Test
    public void testDlsNoRequestPrincipal() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy-dls", uri, false, response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    
    @Test
    public void testNoAttachmentEnabled() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", bigDocUri, true, response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals(2048, response.getContentAsByteArray().length);
        
        byte[] expected = FileUtils.readFileToByteArray(new File("src/test/resources/dummy-search_home/conf/filecopy/shakespeare.html"));
        
        Assert.assertArrayEquals(
            ArrayUtils.subarray(expected, 0, 2048),
            response.getContentAsByteArray());
        Assert.assertEquals("false", response.getHeaderValue("X-Funnelback-DLS"));
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertNull(response.getHeaderValue("Content-Disposition"));
    }

    @Test
    public void testNoAttachmentDisabled() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", bigDocUri, false, response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(new File("src/test/resources/dummy-search_home/conf/filecopy/shakespeare.html")),
            response.getContentAsByteArray());
        Assert.assertEquals("false", response.getHeaderValue("X-Funnelback-DLS"));
        Assert.assertEquals("application/octet-stream", response.getContentType());
        Assert.assertEquals(
            "attachment; filename=\"shakespeare.html\"",
            response.getHeaderValue("Content-Disposition"));
    }

    private static class MockPrincipal implements Principal {
        @Override
        public String getName() {
            return "me";
        }
        
    }
}
