package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.testutils.TmpFolderProvider;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.DefaultPadreForking;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.index.QueryReadLock;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
@Log4j2
public class DefaultPadreForkingLockTests {

    private static final File LOCK_FILE = new File("src/test/resources/dummy-search_home/data/padre-forking/live/idx_update.lock");
    
    @Rule public TestName testName = new TestName();
    
    @Autowired
    private I18n i18n;
    
    @Autowired
    private File searchHome;
    
    private DefaultPadreForking forking;
    
    @Autowired
    private QueryReadLock queryReadLock;
    
    @Before
    public void before() {
        LOCK_FILE.delete();
        Assert.assertFalse("Lock file '"+LOCK_FILE.getAbsolutePath()+"' should have been delete", LOCK_FILE.exists());
        
        forking = new DefaultPadreForking();
        forking.setI18n(i18n);
        forking.setPadreXmlParser(new StaxStreamParser());
        forking.setSearchHome(new File("src/test/resources/dummy-search_home"));
        forking.setQueryReadLock(queryReadLock);
    }
    
    /**
     * Should not throw exception
     * @throws Exception
     */
    @Test
    public void testLockTwoSearches() throws Exception {
        List<String> qpOptions = new ArrayList<String>(Arrays.asList(
            new String[]{"src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml", "2"}));
        
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("padre-forking", 
                new NoOptionsConfig(searchHome, "padre-forking")
            .setValue("query_processor", getMockPadre())));
        qs.getDynamicQueryProcessorOptions().addAll(qpOptions);
        qs.setQuery("test");
                
        final SearchTransaction st = new SearchTransaction(qs, new SearchResponse());
        final DefaultPadreForking forker = forking;

        // Start search in separate thread
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    forker.fetchData(st);
                } catch (DataFetchException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.start();
        
        // Wait a bit so the main search acquire the lock
        Thread.sleep(50);
        
        // Run a second forker with a different query processor, but on the
        // same collection
        DefaultPadreForking forking2 = new DefaultPadreForking();
        forking2.setI18n(i18n);
        forking2.setPadreXmlParser(new StaxStreamParser());
        forking2.setSearchHome(new File("src/test/resources/dummy-search_home"));
        forking2.setQueryReadLock(queryReadLock);

        qpOptions = new ArrayList<String>(Arrays.asList(
            new String[]{
                "src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml"}));

        qs = new SearchQuestion();
        qs.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", getMockPadre())));
        qs.getDynamicQueryProcessorOptions().addAll(qpOptions);
        qs.setQuery("test");
        
        SearchTransaction st2 = null;
        try {
            // Run a second search in the main thread
            st2 = new SearchTransaction(qs, new SearchResponse());
            long start = System.currentTimeMillis();
            forking2.fetchData(st2);
            long elapsed = System.currentTimeMillis()-start;
            
            Assert.assertTrue("We should not have waited at all, but we waited: " + elapsed, elapsed < 1000);
        } finally {
            // Ensure the lock is released for subsequent tests
            t.join();
        }
        
        assertResults(st);
        assertResults(st2);
        
        Assert.assertTrue(LOCK_FILE.exists());
        ensureLockReleased(LOCK_FILE);        
    }

    @Test
    public void testLockCreated() throws DataFetchException, EnvironmentVariableException, IOException {
        List<String> qpOptions = new ArrayList<String>(Arrays.asList(
            new String[]{
                "src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml"}));
        
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", getMockPadre())));
        qs.setQuery("test");
        qs.getDynamicQueryProcessorOptions().addAll(qpOptions);
        SearchTransaction st = new SearchTransaction(qs, new SearchResponse());
        
        forking.fetchData(st);
        assertResults(st);
        
        Assert.assertTrue(LOCK_FILE.exists());
        ensureLockReleased(LOCK_FILE);
    }

    

    @Test(timeout=8000)//mock padre sleeps for 2s
    public void testLockWaits() throws Exception {
        String qp = "mock-padre-wait.sh";
        File tmpDir = TmpFolderProvider.getTmpDir(getClass(), testName);
        File programRunningFile = new File(tmpDir, "started");
        
        List<String> qpOptions = new ArrayList<String>(Arrays.asList(
            new String[]{"src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml", "2", programRunningFile.getAbsolutePath()}));
        
        if (OS.isFamilyWindows()) {
            // Can't sleep/wait in a batch script except when using PING or TIMEOUT,
            // but those weren't working when forked from Java for some reason (error code 9009).
            // I had to create a VBS script, but to run it it needs the full path to the CSCRIPT.EXE
            // interpreter.
            forking.setAbsoluteQueryProcessorPath(true);
            
            qp = System.getenv("SystemRoot") + "\\System32\\cscript.exe";
            
            qpOptions = new ArrayList<String>(Arrays.asList(
                new String[]{
                    "/NoLogo",
                    new File(searchHome, "bin/mock-padre-wait.vbs").getAbsolutePath(),
                    "2",
                    new File(searchHome, "conf/padre-forking/mock-packet.xml").getAbsolutePath(),
                    programRunningFile.getAbsolutePath()}));

        }

        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", qp)));
        qs.getDynamicQueryProcessorOptions().addAll(qpOptions);
        qs.setQuery("test");
                
        final SearchTransaction st = new SearchTransaction(qs, new SearchResponse());
        final DefaultPadreForking forker = forking;

        // Start search in separate thread
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    forker.fetchData(st);
                } catch (DataFetchException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.start();
        
        while(!programRunningFile.exists()) {
            Thread.sleep(10);
        }

        RandomAccessFile raf = new RandomAccessFile(LOCK_FILE, "rw");
        FileChannel channel = raf.getChannel();
        FileLock fl = null;
        try {
            fl = channel.tryLock();
            Assert.fail("Lock should not have been acquired" + fl);
        } catch (OverlappingFileLockException ofle) {
        } finally {
            // Ensure the lock is released for subsequent tests
            t.join();
            if (fl != null) {
                fl.release();
            }
            channel.close();
            raf.close();
        }
        
        assertResults(st);
        Assert.assertTrue(LOCK_FILE.exists());
        ensureLockReleased(LOCK_FILE);        
    }

    private void assertResults(SearchTransaction st) throws IOException {
        Assert.assertNotNull(st.getResponse());
        if (! OS.isFamilyWindows()) {
            // Can't compare results on Windows as the Mock VBS padre
            // doesn't support reading UTF-8
            Assert.assertEquals(FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml")), st.getResponse().getRawPacket());
        }
        Assert.assertEquals(10, st.getResponse().getResultPacket().getResults().size());
        Assert.assertEquals("Online visa applications", st.getResponse().getResultPacket().getResults().get(0).getTitle());    
    }
    
    private void ensureLockReleased(File lockFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
        FileChannel channel = raf.getChannel();
        
        try {
            FileLock fl = channel.tryLock();
            Assert.assertNotNull(fl);
            fl.release();
            Assert.assertFalse(fl.isValid());
        } finally {
            channel.close();
            raf.close();
        }
        
    }
    
    private String getMockPadre() {
        if (OS.isFamilyWindows()) {
            return "readfile.exe";
        } else {
            return "mock-padre-wait.sh";
        }
    }
    
}
