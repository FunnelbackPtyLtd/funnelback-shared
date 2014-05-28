package com.funnelback.publicui.test.search.service.log;

import com.funnelback.common.views.View;
import com.funnelback.common.config.*;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.springmvc.utils.web.LocalHostnameHolder;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LocalLogServiceClickTests extends AbstractLocalLogServiceTests {
    
    
    private File clickLogFileWithHostname = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
            + File.separator + COLLECTION_NAME
            + File.separator + View.live
            + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.CLICKS_LOG_PREFIX 
            + Files.Log.CLICKS_LOG_SEPARATOR + TEST_HOSTNAME 
            + Files.Log.CLICKS_LOG_EXT);
    
    private File clickLogFileNoHostname = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
            + File.separator + COLLECTION_NAME
            + File.separator + View.live
            + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.CLICKS_LOG_PREFIX 
            + Files.Log.CLICKS_LOG_EXT);
    
    private File clickLogFileDoesntExist = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
            + File.separator + UNKNOWN_COLLECTION
            + File.separator + View.live
            + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.CLICKS_LOG_PREFIX 
            + Files.Log.CLICKS_LOG_EXT);
    
    

    @Override
    public void before() {
        if (clickLogFileWithHostname.exists()) {
            Assert.assertTrue(clickLogFileWithHostname.delete());
        }
        if (clickLogFileNoHostname.exists()) {
            Assert.assertTrue(clickLogFileNoHostname.delete());
        }
    }
    
    @Test 
    public void testClickCsvWithHostName() throws Exception {
    	LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
    	when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
    	when(lhh.isLocalhost()).thenReturn(false);
    	logService.setLocalHostnameHolder(lhh);

    	Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME)
    	.setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "true");
    	Collection c = new Collection(COLLECTION_NAME, config);
    	Profile p = new Profile("profile");
    	Date date = new Date(1361331439286L);

    	ClickLog cl = new ClickLog(date, c, p, "192.168.0.1", new URL("http://referrer.com"), 1, new URI("http://example.com/click"), ClickLog.Type.CLICK, null);

    	logService.logClick(cl);
    	String csvWritten = FileUtils.readFileToString(clickLogFileWithHostname);

    	Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK,-\n", 
    			csvWritten);
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
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK,-\n", 
                csvWritten);

        // Now check for append
        logService.logClick(cl);
        csvWritten = FileUtils.readFileToString(clickLogFileNoHostname);
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK,-\nWed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK,-\n", 
                csvWritten);
    }
    
	@Test
	public void testClickCsvNonexistentDirectoryDoesntCrashLoging() throws Exception {
		LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
		when(lhh.getShortHostname()).thenReturn(null);
		logService.setLocalHostnameHolder(lhh);

		Config config = mock(Config.class);
		when(config.getLogDir(View.live)).thenReturn(clickLogFileDoesntExist);
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
    
        ClickLog cl = new ClickLog(date, c, p, "192.168.0.1", new URL("http://referrer.com"), 1, new URI("http://example.com/click"), ClickLog.Type.CLICK, "user-id");
		logService.logClick(cl);
        String csvWritten = FileUtils.readFileToString(clickLogFileNoHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK,user-id\n", 
                csvWritten);
        
        // Now check for append.
        
		logService.logClick(cl);
        csvWritten = FileUtils.readFileToString(clickLogFileNoHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK,user-id\nWed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,1,http://example.com/click,CLICK,user-id\n", 
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
        String csvWritten = FileUtils.readFileToString(clickLogFileNoHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,,1,http://example.com/click,CLICK,-\n", 
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
        String csvWritten = FileUtils.readFileToString(clickLogFileNoHostname);
        
        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,,1,,FP,-\n", 
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
        String csvWritten = FileUtils.readFileToString(clickLogFileNoHostname);
        
        Assert.assertEquals(",,,0,,,-\n", 
                csvWritten);
    }

}
