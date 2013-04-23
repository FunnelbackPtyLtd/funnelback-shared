package com.funnelback.publicui.test.search.service.log;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;
import com.funnelback.publicui.search.service.log.LocalLogService;
import com.funnelback.publicui.utils.web.LocalHostnameHolder;


public class LocalLogServiceTests {

    private static final String UNKNOWN_COLLECTION = "UNKNOWN_COLLECTION";

	private static final String COLLECTION_NAME = "log-service";
    
    private static final File TEST_OUT_ROOT = new File("target" + File.separator
            + "test-response" + File.separator + COLLECTION_NAME); 
    
    private static final File TEST_IN_ROOT = new File("src" + File.separator
            + "test" + File.separator + "resources" + File.separator + COLLECTION_NAME);
    
    private static final String TEST_HOSTNAME = "hostname";
    
    private File contextualNavLogFile = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
            + File.separator + COLLECTION_NAME
            + File.separator + DefaultValues.VIEW_LIVE
            + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.CONTEXTUAL_NAVIGATION_LOG_FILENAME);
    
    private File clickLogFileWithHostname = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
            + File.separator + COLLECTION_NAME
            + File.separator + DefaultValues.VIEW_LIVE
            + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.CLICKS_LOG_PREFIX 
            + Files.Log.CLICKS_LOG_SEPARATOR + TEST_HOSTNAME 
            + Files.Log.CLICKS_LOG_EXT);
    
    private File clickLogFileNoHostname = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
            + File.separator + COLLECTION_NAME
            + File.separator + DefaultValues.VIEW_LIVE
            + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.CLICKS_LOG_PREFIX 
            + Files.Log.CLICKS_LOG_EXT);
    
    private File clickLogFileDoesntExist = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
            + File.separator + UNKNOWN_COLLECTION
            + File.separator + DefaultValues.VIEW_LIVE
            + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.CLICKS_LOG_PREFIX 
            + Files.Log.CLICKS_LOG_EXT);
    
    
    private File publicUiWarningLogFile = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.PUBLIC_UI_WARNINGS_FILENAME);
    
    private LocalLogService logService;
    private LocalHostnameHolder localHostnameHolder;
    
    @BeforeClass
    public static void beforeClass() throws IOException {
        TEST_OUT_ROOT.mkdirs();
        FileUtils.cleanDirectory(TEST_OUT_ROOT);
        FileUtils.copyDirectory(TEST_IN_ROOT, TEST_OUT_ROOT);
    }
    
    @Before
    public void before() {
        localHostnameHolder = new LocalHostnameHolder(); 
        logService = new LocalLogService();
        logService.setLocalHostnameHolder(localHostnameHolder);
        if (contextualNavLogFile.exists()) {
            Assert.assertTrue(contextualNavLogFile.delete());
        }
        if (clickLogFileWithHostname.exists()) {
            Assert.assertTrue(clickLogFileWithHostname.delete());
        }
        if (clickLogFileNoHostname.exists()) {
            Assert.assertTrue(clickLogFileNoHostname.delete());
        }
    }
    
    
    @Test 
    public void testClickCsvNoHostname() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(null);
        
        logService.setLocalHostnameHolder(lhh);
        
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
        Date date = new Date(1361331439286L);
    
        ClickLog cl = new ClickLog(date, c, p, "192.168.0.1", new URL("http://referrer.com"), 1, new URI("http://example.com/click"), ClickLog.Type.CLICK, null);
        
        logService.logClick(cl);
        String csvWritten = FileUtils.readFileToString(clickLogFileNoHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK\n", 
                csvWritten);

        // Now check for append
        logService.logClick(cl);
        csvWritten = FileUtils.readFileToString(clickLogFileNoHostname);
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK\nWed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK\n", 
                csvWritten);
    }
    
	@Test
	public void testClickCsvNonexistentDirectory() throws Exception {
		LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
		when(lhh.getShortHostname()).thenReturn(null);
		logService.setLocalHostnameHolder(lhh);

		Config config = mock(Config.class);
		when(config.getLogDir(DefaultValues.VIEW_LIVE)).thenReturn(clickLogFileDoesntExist);
		Collection c = new Collection(UNKNOWN_COLLECTION, config);
		Profile p = new Profile("profile");
		Date date = new Date(1361331439286L);

		ClickLog cl = new ClickLog(date, c, p, "192.168.0.1", new URL(
				"http://referrer.com"), 1, new URI("http://example.com/click"),
				ClickLog.Type.CLICK, null);

		logService.logClick(cl);
		Assert.assertFalse(clickLogFileDoesntExist.exists());
	}

    
    
    @Test 
    public void testClickCsvComplete() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        logService.setLocalHostnameHolder(lhh);
    	
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
        Date date = new Date(1361331439286L);
    
        ClickLog cl = new ClickLog(date, c, p, "192.168.0.1", new URL("http://referrer.com"), 1, new URI("http://example.com/click"), ClickLog.Type.CLICK, null);
		logService.logClick(cl);
        String csvWritten = FileUtils.readFileToString(clickLogFileWithHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK\n", 
                csvWritten);
        
        // Now check for append.
        
		logService.logClick(cl);
        csvWritten = FileUtils.readFileToString(clickLogFileWithHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK\nWed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK\n", 
                csvWritten);
    }

    @Test 
    public void testClickCsvNoReferrer() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        logService.setLocalHostnameHolder(lhh);
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
        Date date = new Date(1361331439286L);
    
        ClickLog cl = new ClickLog(date, c, p, "192.168.0.1", null, 1, new URI("http://example.com/click"), ClickLog.Type.CLICK, null);
		logService.logClick(cl);
        String csvWritten = FileUtils.readFileToString(clickLogFileWithHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,,1,http://example.com/click,CLICK\n", 
                csvWritten);
    }
    
    @Test 
    public void testClickCsvNoReferrerNoClick() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        logService.setLocalHostnameHolder(lhh);
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
        Date date = new Date(1361331439286L);
    
        ClickLog cl = new ClickLog(date, c, p, "192.168.0.1", null, 1, null, ClickLog.Type.FP, null);
		logService.logClick(cl);
        String csvWritten = FileUtils.readFileToString(clickLogFileWithHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,,1,,FP\n", 
                csvWritten);
    }
    
    @Test 
    public void testClickCsvNull() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        logService.setLocalHostnameHolder(lhh);
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
    
        ClickLog cl = new ClickLog(null, c, p, null, null, 0, null, null, null);
		logService.logClick(cl);
        String csvWritten = FileUtils.readFileToString(clickLogFileWithHostname);
        
        Assert.assertEquals(",,,0,,\n", 
                csvWritten);
    }


    
    @Test
    public void testLogContextualNavigation() throws Exception {
        Date now = new Date();
        
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
        
        ContextualNavigationLog log = new ContextualNavigationLog(
                now,
                c,
                p,
                "userId",
                "cluster",
                Arrays.asList(new String[] {"previousClusters"}),
                null);
        
            
        logService.logContextualNavigation(log);
        
        Assert.assertTrue("Log file should have been created", contextualNavLogFile.exists());
        
        String actual = FileUtils.readFileToString(contextualNavLogFile).replace("\r", "");
        String expected = FileUtils.readFileToString(new File(TEST_IN_ROOT, "expected-cn-log.xml")).replace("\r", "");
        
        expected = expected.replace("{DATE}", ContextualNavigationLog.DATE_FORMAT.format(now));
        
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testLogContextualNavigationThreadSafe() throws Exception {
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        final Collection c = new Collection(COLLECTION_NAME, config);
        final Profile p = new Profile("profile");
        final ContextualNavigationLog log = new ContextualNavigationLog(
                new Date(),
                c,
                p,
                "userId",
                "cluster",
                Arrays.asList(new String[] {"previousClusters"}),
                null);
                
        for (int i=0; i<250; i++) {
            new Runnable() {
                @Override
                public void run() {
                    logService.logContextualNavigation(log);
                }
            }.run();
        }
        
        Assert.assertTrue("Log file should have been created", contextualNavLogFile.exists());

        // Try XML parsing to assess XML syntax compliance
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document d = factory.newDocumentBuilder().parse(contextualNavLogFile);
        
        NodeList childs = d.getElementsByTagName("cflus");
        Assert.assertEquals(250, childs.getLength());        
    }
    
    @Test
    public void testNoCollection() throws Exception {
        final Profile p = new Profile("profile");
        final ContextualNavigationLog log = new ContextualNavigationLog(
                new Date(),
                null,
                p,
                "userId",
                "cluster",
                Arrays.asList(new String[] {"previousClusters"}),
                null);
        
        logService.logContextualNavigation(log);
        
        Assert.assertFalse("Log file should not have been created", contextualNavLogFile.exists());
    }
    
    @Test
    public void testLogFileWithTrailingNewLine() throws Exception {
        Date now = new Date();
        
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
        
        File existingLog = new File(TEST_IN_ROOT, "log-with-trailing-newline.xml");
        FileUtils.copyFile(existingLog, contextualNavLogFile);
        
        ContextualNavigationLog log = new ContextualNavigationLog(
                now,
                c,
                p,
                "userId",
                "cluster",
                Arrays.asList(new String[] {"previousClusters"}),
                null);
        
            
        logService.logContextualNavigation(log);
        
        Assert.assertTrue("Log file should have been created", contextualNavLogFile.exists());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document d = factory.newDocumentBuilder().parse(contextualNavLogFile);
        
        NodeList childs = d.getElementsByTagName("cflus");
        Assert.assertEquals(2, childs.getLength());
    }
    
    @Test
    public void testInvalidLog() throws Exception {
        Date now = new Date();
        
        NoOptionsConfig config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME);
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
        
        File existingLog = new File(TEST_IN_ROOT, "invalid-log.xml");
        FileUtils.copyFile(existingLog, contextualNavLogFile);
        
        ContextualNavigationLog log = new ContextualNavigationLog(
                now,
                c,
                p,
                "userId",
                "cluster",
                Arrays.asList(new String[] {"previousClusters"}),
                null);
        
            
        logService.logContextualNavigation(log);

        String actual = FileUtils.readFileToString(contextualNavLogFile).replace("\r", "");
        String expected = FileUtils.readFileToString(new File(TEST_IN_ROOT, "invalid-log.xml")).replace("\r", "");
        
        Assert.assertEquals("Invalid log shouldn't have been updated", expected, actual);
    }

    @Test
    public void testLogPublicUIWarning() throws Exception {
        NoOptionsConfig config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME);
        Collection c = new Collection(COLLECTION_NAME, config);

        Date now = new Date();
        PublicUIWarningLog warning = new PublicUIWarningLog(now,
                c,
                null,
                null,
                "Test message",
                null);
        
        logService.setSearchHome(TEST_OUT_ROOT);
        logService.logPublicUIWarning(warning);
        
        String actual = FileUtils.readFileToString(publicUiWarningLogFile);
        String expected = PublicUIWarningLog.DATE_FORMAT.format(now) + " " + c.getId() + " - Test message\n";
        
        Assert.assertEquals(expected, actual);
        
        // Append another message
        now = new Date();
        warning = new PublicUIWarningLog(now,
                c,
                null,
                null,
                "Second message",
                null);
        
        logService.logPublicUIWarning(warning);
        
        actual = FileUtils.readFileToString(publicUiWarningLogFile);
        expected += PublicUIWarningLog.DATE_FORMAT.format(now) + " " + c.getId() + " - Second message\n";
        
        Assert.assertEquals(expected, actual);
        
    }

    @Test
    public void testHostnameInFilename() throws IOException {

        File contextualNavLogFile = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
                + File.separator + COLLECTION_NAME
                + File.separator + DefaultValues.VIEW_LIVE
                + File.separator + DefaultValues.FOLDER_LOG,
                Files.Log.CONTEXTUAL_NAVIGATION_LOG_PREFIX + "-"
                + localHostnameHolder.getShortHostname()
                + Files.Log.CONTEXTUAL_NAVIGATION_LOG_EXT);
        
        if (contextualNavLogFile.exists()) {
            Assert.assertTrue(contextualNavLogFile.delete());
        }

        Date now = new Date();
            
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
            .setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "true");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");
        
        ContextualNavigationLog log = new ContextualNavigationLog(
                now,
                c,
                p,
                "userId",
                "cluster",
                Arrays.asList(new String[] {"previousClusters"}),
                null);
        
            
        logService.logContextualNavigation(log);

        Assert.assertTrue("Log file should have been created", contextualNavLogFile.exists());
        
        String actual = FileUtils.readFileToString(contextualNavLogFile).replace("\r", "");
        String expected = FileUtils.readFileToString(new File(TEST_IN_ROOT, "expected-cn-log.xml")).replace("\r", "");
        
        expected = expected.replace("{DATE}", ContextualNavigationLog.DATE_FORMAT.format(now));
        
        Assert.assertEquals(expected, actual);
    }
    
}
