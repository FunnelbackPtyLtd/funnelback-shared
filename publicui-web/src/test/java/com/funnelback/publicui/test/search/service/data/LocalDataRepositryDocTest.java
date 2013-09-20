package com.funnelback.publicui.test.search.service.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.Xml;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.Store.View;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.data.LocalDataRepository;

public class LocalDataRepositryDocTest {

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
            collection, View.live, "http://invalid.url/file.html",
            "sub-folder/cached-doc.txt", 0, -1);
        
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
            collection, View.live, "http://invalid.url/file.html",
            "sub-folder/cached-doc.xml", 0, -1);
        
        Assert.assertNotNull(rmd);
        Assert.assertNotNull(rmd.record);
        Assert.assertTrue(rmd.record instanceof XmlRecord);
        Assert.assertNotNull(rmd.metadata);
        
        Assert.assertEquals(
            FileUtils.readFileToString(new File(SEARCH_HOME, "data/data-repository/live/data/sub-folder/cached-doc.xml")),
            Xml.toString(((XmlRecord) rmd.record).getContent()));
        Assert.assertEquals(0, rmd.metadata.size());
    }
    
    /**
     * WARC not supported and should return null
     * @see FUN-5956
     */
    @Test
    public void testWarc() throws IOException {
        RecordAndMetadata<? extends Record<?>> rmd = repository.getDocument(
            collection, View.live, "http://invalid.url/file.html",
            "sub-folder/cached-doc.warc", 0, -1);
        
        Assert.assertNull(rmd);
    }

    @Test
    public void testInvalidFile() throws IOException {
        RecordAndMetadata<? extends Record<?>> rmd = repository.getDocument(
            collection, View.live, "http://invalid.url/file.html",
            "invalid-file.txt", 0, -1);
        
        Assert.assertNull(rmd);
    }


}
