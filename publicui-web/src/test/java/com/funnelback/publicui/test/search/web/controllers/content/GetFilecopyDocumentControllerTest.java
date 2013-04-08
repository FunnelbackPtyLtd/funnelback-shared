package com.funnelback.publicui.test.search.web.controllers.content;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
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

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
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
    
    @Autowired
    private File searchHome;
    
    @Autowired
    private AuthTokenManager authTokenManager;

    private URI uri;
    private URI bigDocUri;
    private URI invalidUri;

    @Before
    public void before() throws Exception {
        configRepository.setGlobalConfiguration(new GlobalOnlyConfig(searchHome));
        configRepository.getGlobalConfiguration().setValue(Keys.SERVER_SECRET, "autotest-server-secret");
        
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
        controller.getFilecopyDocument("non-existent", null, false, "token", response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
    
    @Test
    public void testWrongCollectionType() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("dummy", invalidUri, false, "token", response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    @Test(expected=FileSystemException.class)
    public void testNonExistentUriNoDls() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", invalidUri, false, tokenize(invalidUri), response, new MockHttpServletRequest());
    }
    
    @Test
    public void testNoDls() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", uri, false, tokenize(uri), response, new MockHttpServletRequest());
        
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
            
            controller.getFilecopyDocument("filecopy-dls", uri, false, tokenize(uri), response, request);
            
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
            
            controller.getFilecopyDocument("filecopy-dls", uri, false, tokenize(uri), response, request);
            
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
            
            controller.getFilecopyDocument("filecopy-dls", uri, false, tokenize(uri), response, request);
            
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
        controller.getFilecopyDocument("filecopy-dls", uri, false, tokenize(uri), response, request);
        
        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    
    @Test
    public void testDlsNoRequestPrincipal() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy-dls", uri, false, tokenize(uri), response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    
    @Test
    public void testNoAttachmentEnabled() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", 
            new URI(bigDocUri.toString().replaceAll("\\.html", ".doc")), true,
                tokenize(new URI(bigDocUri.toString().replaceAll("\\.html", ".doc"))), response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals(2048, response.getContentAsByteArray().length);
        
        byte[] expected = FileUtils.readFileToByteArray(new File("src/test/resources/dummy-search_home/conf/filecopy/shakespeare.doc"));
        
        Assert.assertArrayEquals(
            ArrayUtils.subarray(expected, 0, 2048),
            response.getContentAsByteArray());
        Assert.assertEquals("false", response.getHeaderValue("X-Funnelback-DLS"));
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertNull(response.getHeaderValue("Content-Disposition"));
    }

    @Test
    public void testNoAttachmentDisabledButHtml() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", bigDocUri, false, tokenize(bigDocUri), response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(new File("src/test/resources/dummy-search_home/conf/filecopy/shakespeare.html")),
            response.getContentAsByteArray());
        Assert.assertEquals("false", response.getHeaderValue("X-Funnelback-DLS"));
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertNull(response.getHeaderValue("Content-Disposition"));
    }

    @Test    
    public void testNoAttachmentDisabled() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy",
            new URI(bigDocUri.toString().replaceAll("\\.html", ".txt")),false,
                tokenize(new URI(bigDocUri.toString().replaceAll("\\.html", ".txt"))), response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(new File("src/test/resources/dummy-search_home/conf/filecopy/shakespeare.txt")),
            response.getContentAsByteArray());
        Assert.assertEquals("false", response.getHeaderValue("X-Funnelback-DLS"));
        Assert.assertEquals("application/octet-stream", response.getContentType());
        Assert.assertEquals(
            "attachment; filename=\"shakespeare.txt\"",
            response.getHeaderValue("Content-Disposition"));
    }
    
    @Test
    public void testWrongToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getFilecopyDocument("filecopy", uri, false, "wrong-token", response, new MockHttpServletRequest());
        
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        Assert.assertEquals("serve.bad_token", response.getContentAsString());
    }
    
    @Test
    public void testTokenUrlDecode() throws Exception {
        URI uriShakespeare = new URI("smb://internalfilesha/DLS%20Share/Shakespeare/romeo_juliet/romeo_juliet.1.3.html");
        String tokenShakespeare = tokenize(uriShakespeare);
        Assert.assertTrue( "Token should contain a plus but was: "+tokenShakespeare, tokenShakespeare.contains("+"));
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        try {
            controller.getFilecopyDocument("filecopy",
                uriShakespeare, false, tokenShakespeare, response, new MockHttpServletRequest());
            Assert.fail();
        } catch (FileSystemException fse) {
           // Thrown because we can't access the smb:// file from  unit test,
           // that's ok
           Assert.assertTrue(fse.getMessage().contains("Could not determine the type of file \"smb://"));
        }
    }
    
    @Test
    public void testUrlWithPlus() throws Exception {
        URI uriWithPlus = new URI("smb://internalfilesha/share/bad_characters/%2Bplus.html");
        String token = tokenize(uriWithPlus);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        try {
            controller.getFilecopyDocument("filecopy",
                uriWithPlus, false, token, response, new MockHttpServletRequest());
            Assert.fail();
        } catch (FileSystemException fse) {
           // Thrown because we can't access the smb:// file from  unit test,
           // that's ok
           Assert.assertTrue(fse.getMessage().contains("Could not determine the type of file \"smb://"));
        }
    }

    private String tokenize(URI uri) throws UnsupportedEncodingException {
        return authTokenManager.getToken(URLDecoder.decode(uri.toString(), "UTF-8"), "autotest-server-secret");
    }
    
    private static class MockPrincipal implements Principal {
        @Override
        public String getName() {
            return "me";
        }
    }
}
