package com.funnelback.publicui.test.search.service.log;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;

import com.funnelback.publicui.search.service.log.LocalLogService;
import com.funnelback.springmvc.utils.web.LocalHostnameHolder;

public abstract class AbstractLocalLogServiceTests {

    protected static final String UNKNOWN_COLLECTION = "UNKNOWN_COLLECTION";

    protected static final String COLLECTION_NAME = "log-service";
    
    protected static final File TEST_IN_ROOT = new File("src" + File.separator
        + "test" + File.separator + "resources" + File.separator + COLLECTION_NAME);
    
    protected static final File TEST_OUT_ROOT = new File("target" + File.separator
            + "test-log-service" + File.separator + COLLECTION_NAME); 
    
    protected static final String TEST_HOSTNAME = "hostname";

    protected LocalLogService logService;
    protected LocalHostnameHolder localHostnameHolder;
    
    @BeforeClass
    public static void beforeClass() throws IOException {
        TEST_OUT_ROOT.mkdirs();
        FileUtils.cleanDirectory(TEST_OUT_ROOT);
        FileUtils.copyDirectory(TEST_IN_ROOT, TEST_OUT_ROOT);
    }
    
    @Before
    public void beforeAll() throws Exception {
        LocalHostnameHolder lhh = mock(LocalHostnameHolder.class);
        when(lhh.getShortHostname()).thenReturn("mock-hostname");
        when(lhh.getHostname()).thenReturn("mock-hostname.domain.com");

        localHostnameHolder = lhh;
        logService = new LocalLogService();
        logService.setLocalHostnameHolder(localHostnameHolder);
        before();
    }
    
    protected abstract void before() throws Exception;
    
    

}
