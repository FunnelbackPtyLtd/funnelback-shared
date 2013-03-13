package com.funnelback.publicui.test.search.service.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileSystemException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.data.LocalDataRepository;

public class LocalDataRepositoryFilecopyTest {

    private static final File TEST_FILE = new File("src/test/resources/dummy-search_home/conf/filecopy/collection.cfg");
    
    private Collection collection;
    
    private URI uri;
    private URI invalidUri;
    
    @Before
    public void before() throws Exception {
        collection = new Collection("filecopy",
            new NoOptionsConfig(new File("src/test/resources/dummy-search_home"),
                "filecopy")
            .setValue(Keys.FileCopy.USERNAME, "")
            .setValue(Keys.FileCopy.PASSWORD, "")
            .setValue(Keys.FileCopy.DOMAIN, ""));
        
        if (OS.isFamilyWindows()) {
            uri = new URI("file:///"+TEST_FILE.getAbsolutePath().replace("\\", "/"));
            invalidUri = new URI("file:///C:/non-existent.ext");
        } else {
            uri = new URI("file://"+TEST_FILE.getAbsolutePath());
            invalidUri = new URI("file:///non-existent/file.ext");
        }
    }
    
    @Test(expected=FileSystemException.class)
    public void testNonExistentFile() throws Exception {
        new LocalDataRepository()
            .getFilecopyDocument(collection, invalidUri, false);
    }
    
    @Test(expected=FileNotFoundException.class)
    public void testNonExistentFileDls() throws Exception {
        Assume.assumeTrue(OS.isFamilyWindows());
        
        new LocalDataRepository()
        .getFilecopyDocument(collection, invalidUri, true);
    }
    
    @Test
    public void testNoDls() throws Exception {
        InputStream is = new LocalDataRepository()
            .getFilecopyDocument(collection, uri, false);
        
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(TEST_FILE),
            IOUtils.toByteArray(is));
    }
    
    @Test
    public void testDls() throws Exception {
        Assume.assumeTrue(OS.isFamilyWindows());
        
        InputStream is = new LocalDataRepository()
        .getFilecopyDocument(collection, uri, true);
    
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(TEST_FILE),
            IOUtils.toByteArray(is));
    }
    
}