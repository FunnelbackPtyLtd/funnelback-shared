package com.funnelback.publicui.test.search.service.log;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.utils.web.LocalHostnameHolder;

public abstract class AbstractLocalLogServiceXmlTests extends AbstractLocalLogServiceTests {

    private Config config;
    private Collection c;
    protected Profile p;
    
    @Override
    public void before() throws IOException {
        if (getLogFile().exists()) {
            Assert.assertTrue(getLogFile().delete());
        }
        
        config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME);
        c = new Collection(COLLECTION_NAME, config);
        p = new Profile("profile");
    }
    
    @Test
    public void testLogWithHostName() throws Exception {
    	LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
    	when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
    	when(lhh.isLocalhost()).thenReturn(false);
    	logService.setLocalHostnameHolder(lhh);
        config.setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "true");
 
        Date now = new Date();
        log(getLog(c, now));
        Assert.assertTrue("Log file should have been created", getLogFile(TEST_HOSTNAME).exists());
        
        String actual = FileUtils.readFileToString(getLogFile(TEST_HOSTNAME)).replace("\r", "");
        String expected = FileUtils.readFileToString(new File(getTestResourcesFolder(), "expected-log.xml")).replace("\r", "");
        expected = expected.replace("{DATE}", Log.XML_DATE_FORMAT.format(now));
        
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testLog() throws Exception {
        config.setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
 
        Date now = new Date();
        log(getLog(c, now));
        Assert.assertTrue("Log file should have been created", getLogFile().exists());
        
        String actual = FileUtils.readFileToString(getLogFile()).replace("\r", "");
        String expected = FileUtils.readFileToString(new File(getTestResourcesFolder(), "expected-log.xml")).replace("\r", "");
        expected = expected.replace("{DATE}", Log.XML_DATE_FORMAT.format(now));
        
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testLogThreadSafe() throws Exception {
        config.setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        final Log log = getLog(c, new Date());
        
        for (int i=0; i<250; i++) {
            new Runnable() {
                @Override
                public void run() {
                    log(log);
                }
            }.run();
        }
        
        Assert.assertTrue("Log file should have been created", getLogFile().exists());

        // Try XML parsing to assess XML syntax compliance
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document d = factory.newDocumentBuilder().parse(getLogFile());
        
        NodeList childs = d.getElementsByTagName(getRootTag());
        Assert.assertEquals(250, childs.getLength());        
    }

    @Test
    public void testInvalidLog() throws Exception {
        File existingLog = new File(getTestResourcesFolder(), "invalid-log.xml");
        FileUtils.copyFile(existingLog, getLogFile());
        
        log(getLog(c, new Date()));

        String actual = FileUtils.readFileToString(getLogFile()).replace("\r", "");
        String expected = FileUtils.readFileToString(new File(getTestResourcesFolder(), "invalid-log.xml")).replace("\r", "");
        
        Assert.assertEquals("Invalid log shouldn't have been updated", expected, actual);
    }

    @Test
    public void testLogFileWithTrailingNewLine() throws Exception {
        config.setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        
        File existingLog = new File(getTestResourcesFolder(), "log-with-trailing-newline.xml");
        FileUtils.copyFile(existingLog, getLogFile());
        
        log(getLog(c, new Date()));
        
        Assert.assertTrue("Log file should have been created", getLogFile().exists());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document d = factory.newDocumentBuilder().parse(getLogFile());
        
        NodeList childs = d.getElementsByTagName(getRootTag());
        Assert.assertEquals(2, childs.getLength());
    }

    @Test
    public void testNoCollection() throws Exception {
        log(getLog(c, new Date()));
        Assert.assertFalse("Log file should not have been created", getLogFile().exists());
    }
    
    @Test
    public void testHostnameInFilename() throws IOException {
        config.setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "true");
        
        File logFile = getLogFile(localHostnameHolder.getShortHostname());
        if (logFile.exists()) {
            Assert.assertTrue(logFile.delete());
        }

        Date now = new Date();
        log(getLog(c, now));
        Assert.assertTrue("Log file should have been created", logFile.exists());
        
        String actual = FileUtils.readFileToString(logFile).replace("\r", "");
        String expected = FileUtils.readFileToString(new File(getTestResourcesFolder(), "expected-log.xml")).replace("\r", "");
        expected = expected.replace("{DATE}", Log.XML_DATE_FORMAT.format(now));
        Assert.assertEquals(expected, actual);
    }
    
    protected File getLogFile() {
        return getLogFile(null);
    }
    
    protected File getLogFile(String hostname) {
        String fileName = getLogPrefix() + getLogSuffix();
        if (hostname != null) {
            fileName = getLogPrefix() + "-"
                + hostname
                + getLogSuffix();
        }
        
        return new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
            + File.separator + COLLECTION_NAME
            + File.separator + DefaultValues.VIEW_LIVE
            + File.separator + DefaultValues.FOLDER_LOG,
            fileName);
    }
    
    protected abstract Log getLog(Collection collection, Date d);
    
    protected abstract String getLogPrefix();
    
    protected abstract String getLogSuffix();
    
    protected abstract void log(Log l);
    
    protected abstract String getRootTag();
    
    protected abstract File getTestResourcesFolder();

}
