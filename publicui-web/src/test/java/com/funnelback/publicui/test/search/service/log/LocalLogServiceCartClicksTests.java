package com.funnelback.publicui.test.search.service.log;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.views.View;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.springmvc.utils.web.LocalHostnameHolder;

public class LocalLogServiceCartClicksTests extends AbstractLocalLogServiceTests {

    private File cartLogFileWithHostname = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
        + File.separator + COLLECTION_NAME + File.separator + View.live + File.separator + DefaultValues.FOLDER_LOG,
        Files.Log.CART_CLICKS_LOG_PREFIX + Files.Log.CART_CLICKS_LOG_SEPARATOR + TEST_HOSTNAME
            + Files.Log.CART_CLICKS_LOG_EXT);

    private File cartLogFileNoHostname = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
        + File.separator + COLLECTION_NAME + File.separator + View.live + File.separator + DefaultValues.FOLDER_LOG,
        Files.Log.CART_CLICKS_LOG_PREFIX + Files.Log.CART_CLICKS_LOG_EXT);

    private File cartLogFileDoesntExist = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
        + File.separator + UNKNOWN_COLLECTION + File.separator + View.live + File.separator + DefaultValues.FOLDER_LOG,
        Files.Log.CART_CLICKS_LOG_PREFIX + Files.Log.CART_CLICKS_LOG_EXT);

    private Date date;

    @Override
    public void before() throws Exception {
        if (cartLogFileWithHostname.exists()) {
            Assert.assertTrue(cartLogFileWithHostname.delete());
        }
        if (cartLogFileNoHostname.exists()) {
            Assert.assertTrue(cartLogFileNoHostname.delete());
        }

        date = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH).parse("Wed Feb 20 14:37:19 2013");
    }

    @Test
    public void testCartCsvWithHostName() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        when(lhh.isLocalhost()).thenReturn(false);
        logService.setLocalHostnameHolder(lhh);

        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME).setValue(Keys.Logging.HOSTNAME_IN_FILENAME,
            "true");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");

        CartClickLog cl = new CartClickLog(date, c, p, "192.168.0.1", new URL("http://referrer.com"), new URI(
            "http://example.com/cart"), CartClickLog.Type.ADD_TO_CART, null);

        logService.logCart(cl);
        String csvWritten = FileUtils.readFileToString(cartLogFileWithHostname);

        Assert.assertEquals(
            "Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,http://example.com/cart,ADD_TO_CART,-\n",
            csvWritten);
    }

    @Test
    public void testCartCsvNoHostname() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(null);

        logService.setLocalHostnameHolder(lhh);

        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME).setValue(Keys.Logging.HOSTNAME_IN_FILENAME,
            "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");

        CartClickLog cl = new CartClickLog(date, c, p, "192.168.0.1", new URL("http://referrer.com"), new URI(
            "http://example.com/cart"), CartClickLog.Type.ADD_TO_CART, null);

        logService.logCart(cl);
        String csvWritten = FileUtils.readFileToString(cartLogFileNoHostname);

        Assert.assertEquals(
            "Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,http://example.com/cart,ADD_TO_CART,-\n",
            csvWritten);

        // Now check for append
        logService.logCart(cl);
        csvWritten = FileUtils.readFileToString(cartLogFileNoHostname);
        Assert
            .assertEquals(
                "Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,http://example.com/cart,ADD_TO_CART,-\nWed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,http://example.com/cart,ADD_TO_CART,-\n",
                csvWritten);
    }

    @Test
    public void testCartCsvNonexistentDirectoryDoesntCrashLoging() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(null);
        logService.setLocalHostnameHolder(lhh);

        Config config = mock(Config.class);
        when(config.getLogDir(View.live)).thenReturn(cartLogFileDoesntExist);
        Collection c = new Collection(UNKNOWN_COLLECTION, config);
        Profile p = new Profile("profile");

        CartClickLog cl = new CartClickLog(date, c, p, "192.168.0.1", new URL("http://referrer.com"), new URI(
            "http://example.com/cart"), CartClickLog.Type.ADD_TO_CART, null);

        logService.logCart(cl);
        Assert.assertFalse(cartLogFileDoesntExist.exists());
    }

    @Test
    public void testCartCsvComplete() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        logService.setLocalHostnameHolder(lhh);

        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME).setValue(Keys.Logging.HOSTNAME_IN_FILENAME,
            "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");

        CartClickLog cl = new CartClickLog(date, c, p, "192.168.0.1", new URL("http://referrer.com"), new URI(
            "http://example.com/cart"), CartClickLog.Type.ADD_TO_CART, "user-id");
        logService.logCart(cl);
        String csvWritten = FileUtils.readFileToString(cartLogFileNoHostname);

        Assert.assertEquals(
            "Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,http://example.com/cart,ADD_TO_CART,user-id\n",
            csvWritten);

        // Now check for append.

        logService.logCart(cl);
        csvWritten = FileUtils.readFileToString(cartLogFileNoHostname);

        Assert
            .assertEquals(
                "Wed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,http://example.com/cart,ADD_TO_CART,user-id\nWed Feb 20 14:37:19 2013,192.168.0.1,http://referrer.com,http://example.com/cart,ADD_TO_CART,user-id\n",
                csvWritten);
    }

    @Test
    public void testCartCsvNoReferrer() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        logService.setLocalHostnameHolder(lhh);
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME).setValue(Keys.Logging.HOSTNAME_IN_FILENAME,
            "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");

        CartClickLog cl = new CartClickLog(date, c, p, "192.168.0.1", null, new URI("http://example.com/cart"),
            CartClickLog.Type.ADD_TO_CART, null);
        logService.logCart(cl);
        String csvWritten = FileUtils.readFileToString(cartLogFileNoHostname);

        Assert
            .assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,,http://example.com/cart,ADD_TO_CART,-\n", csvWritten);
    }

    @Test
    public void testCartCsvNoReferrerNoCart() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        logService.setLocalHostnameHolder(lhh);
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME).setValue(Keys.Logging.HOSTNAME_IN_FILENAME,
            "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");

        CartClickLog cl = new CartClickLog(date, c, p, "192.168.0.1", null, null, CartClickLog.Type.ADD_TO_CART, null);
        logService.logCart(cl);
        String csvWritten = FileUtils.readFileToString(cartLogFileNoHostname);

        Assert.assertEquals("Wed Feb 20 14:37:19 2013,192.168.0.1,,,ADD_TO_CART,-\n", csvWritten);
    }

    @Test
    public void testCartCsvNull() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn(TEST_HOSTNAME);
        logService.setLocalHostnameHolder(lhh);
        Config config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME).setValue(Keys.Logging.HOSTNAME_IN_FILENAME,
            "false");
        Collection c = new Collection(COLLECTION_NAME, config);
        Profile p = new Profile("profile");

        CartClickLog cl = new CartClickLog(null, c, p, null, null, null, null, null);
        logService.logCart(cl);
        String csvWritten = FileUtils.readFileToString(cartLogFileNoHostname);

        Assert.assertEquals(",,,,,-\n", csvWritten);
    }

}
