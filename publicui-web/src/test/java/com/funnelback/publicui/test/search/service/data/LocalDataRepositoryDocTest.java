package com.funnelback.publicui.test.search.service.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.utils.XMLUtils;
import com.funnelback.common.views.StoreView;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.data.LocalDataRepository;

public class LocalDataRepositoryDocTest {

    private static final File SEARCH_HOME = new File("src/test/resources/dummy-search_home");
    
    private LocalDataRepository repository;
    private Collection collection;
    
    @Before
    public void before() throws FileNotFoundException {
        collection = new Collection("data-repository", new NoOptionsConfig(SEARCH_HOME, "data-repository"));
        repository = new LocalDataRepository(SEARCH_HOME);
    }
    
    @Test
    public void testTxt() throws IOException {
        RecordAndMetadata<? extends Record<?>> rmd = repository.getDocument(
            collection, StoreView.live, "http://invalid.url/file.html",
            new File("sub-folder/cached-doc.txt"), 0, -1);
        
        Assert.assertNotNull(rmd);
        Assert.assertNotNull(rmd.record);
        Assert.assertTrue(rmd.record instanceof RawBytesRecord);
        Assert.assertNotNull(rmd.metadata);
        
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(new File(SEARCH_HOME, "data/data-repository/live/data/sub-folder/cached-doc.txt")),
            ((RawBytesRecord) rmd.record).getContent());
        Assert.assertEquals(0, rmd.metadata.size());
    }

    @Test
    public void testXml() throws IOException {
        RecordAndMetadata<? extends Record<?>> rmd = repository.getDocument(
            collection, StoreView.live, "http://invalid.url/file.html",
            new File("sub-folder/cached-doc.xml"), 0, -1);
        
        Assert.assertNotNull(rmd);
        Assert.assertNotNull(rmd.record);
        Assert.assertTrue(rmd.record instanceof XmlRecord);
        Assert.assertNotNull(rmd.metadata);
        
        Assert.assertEquals(
            FileUtils.readFileToString(new File(SEARCH_HOME, "data/data-repository/live/data/sub-folder/cached-doc.xml"))
                .replace("\r", "")
                .replaceAll("\n\\s*", "\n"),
            XMLUtils.toString(((XmlRecord) rmd.record).getContent())
                .replace("\r", "")
                .replaceAll("\n\\s*", "\n"));
        Assert.assertEquals(0, rmd.metadata.size());
    }
    
    /**
     * WARC not supported and should return null
     * @see FUN-5956
     */
    @Test
    public void testWarc() throws IOException {
        RecordAndMetadata<? extends Record<?>> rmd = repository.getDocument(
            collection, StoreView.live, "http://invalid.url/file.html",
            new File("sub-folder/cached-doc.warc"), 0, -1);
        
        Assert.assertNull(rmd);
    }

    @Test
    public void testInvalidFile() throws IOException {
        RecordAndMetadata<? extends Record<?>> rmd = repository.getDocument(
            collection, StoreView.live, "http://invalid.url/file.html",
            new File("invalid-file.txt"), 0, -1);
        
        Assert.assertNull(rmd);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testParentPath() throws IOException {
        repository.getDocument(collection, StoreView.live, "http://ignored.url/",
            new File("../file-outside-data.txt"), 0, -1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSneakyParentPath() throws IOException {
        repository.getDocument(collection, StoreView.live, "http://ignored.url/",
            new File("folder/file/../../../file-outside-data.txt"), 0, -1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAbsolutePath() throws IOException {
        if (OS.isFamilyWindows()) {
            repository.getDocument(collection, StoreView.live, "http://ignored.url/",
                new File("C:\\Windows\\System32\\cmd.exe"), 0, -1);
        } else {
            repository.getDocument(collection, StoreView.live, "http://ignored.url/",
                new File("/etc/passwd"), 0, -1);
        }
    }
    
    @Test
    public void testSecondaryData() throws IOException {
        RecordAndMetadata<? extends Record<?>> rmd = repository.getDocument(
            collection, StoreView.live, "http://invalid.url/file.html",
            new File("cached-doc.secondary-data.txt"), 0, -1);
        
        Assert.assertNotNull(rmd);
        Assert.assertNotNull(rmd.record);
        Assert.assertTrue(rmd.record instanceof RawBytesRecord);
        Assert.assertNotNull(rmd.metadata);
        
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(new File(SEARCH_HOME, "data/data-repository/live/secondary-data/cached-doc.secondary-data.txt")),
            ((RawBytesRecord) rmd.record).getContent());
        Assert.assertEquals(0, rmd.metadata.size());
    }

    /**
     * The most recent version of the document is always in the secondary-data folder
     */
    @Test
    public void testSecondaryAndMainData() throws IOException {
        RecordAndMetadata<? extends Record<?>> rmd = repository.getDocument(
            collection, StoreView.live, "http://invalid.url/file.html",
            new File("cached-doc.in-both-data.txt"), 0, -1);
        
        Assert.assertNotNull(rmd);
        Assert.assertNotNull(rmd.record);
        Assert.assertTrue(rmd.record instanceof RawBytesRecord);
        Assert.assertNotNull(rmd.metadata);
        
        Assert.assertArrayEquals(
            FileUtils.readFileToByteArray(new File(SEARCH_HOME, "data/data-repository/live/secondary-data/cached-doc.in-both-data.txt")),
            ((RawBytesRecord) rmd.record).getContent());
        Assert.assertEquals(0, rmd.metadata.size());
    }


}
