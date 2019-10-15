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
            .setValue(com.funnelback.config.keys.Keys.CollectionKeys.FilecopyGatherer.PASSWORD.getKey(), "")
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
        new LocalDataRepository(new File("src/test/resources/dummy-search_home"))
            .getFilecopyDocument(collection, invalidUri, false);
    }
    
    @Test
    public void testNonExistentFileDls() throws Exception {
        if (OS.isFamilyWindows()) {
            try {
                new LocalDataRepository(new File("src/test/resources/dummy-search_home"))
                    .getFilecopyDocument(collection, invalidUri, true);
                Assert.fail("Should have thrown a " + FileNotFoundException.class);
            } catch (FileNotFoundException fnfe) {
            }
        }
    }
    
    @Test
    public void testNoDls() throws Exception {
        InputStream is = new LocalDataRepository(new File("src/test/resources/dummy-search_home"))
            .getFilecopyDocument(collection, uri, false);
        
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(TEST_FILE),
            IOUtils.toByteArray(is));
    }
    
    @Test
    public void testDls() throws Exception {
        if (OS.isFamilyWindows()) {
        
            InputStream is = new LocalDataRepository(new File("src/test/resources/dummy-search_home"))
                .getFilecopyDocument(collection, uri, true);
        
            Assert.assertArrayEquals(
                FileUtils.readFileToByteArray(TEST_FILE),
                IOUtils.toByteArray(is));
        }
    }
    
}
