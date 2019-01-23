package com.funnelback.publicui.test.search.service.config;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.xml.DefaultFacetedNavigationConfigParser;
import com.funnelback.springmvc.service.resource.AutoRefreshResourceManager;
import com.funnelback.springmvc.utils.ConfFileService;

import net.sf.ehcache.CacheManager;

public abstract class DefaultConfigRepositoryTestBase {

    private File DUMMY_SEARCH_HOME = new File("src/test/resources/dummy-search_home");
    protected File SEARCH_HOME = new File("target/test-output/config-repository");
    protected File TEST_DIR = new File(SEARCH_HOME, "conf/config-repository");
    
    protected WaitConfigRepository configRepository;
    private AutoRefreshResourceManager resourceManager;
    
    @Autowired
    private CacheManager appCacheManager;
    
    @Autowired
    private ConfFileService fileService;
    
    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;
    
    /** A counter of when we modify a file so that we always increase the modified time of a file
     * by at least one second */
    public static final AtomicLong modifiedTimes = new AtomicLong(0);

    /**
     * Create fake SEARCH_HOME in target/
     * as we'll be fiddling with files
     */
    @Before
    public void before() throws IOException {
        FileUtils.deleteDirectory(SEARCH_HOME);
        TEST_DIR.mkdirs();
        FileUtils.copyDirectory(new File(DUMMY_SEARCH_HOME+"/conf/config-repository"), TEST_DIR);
        for (String s: new String[] {"conf/collection.cfg.default", "conf/global.cfg.default"}) {
            FileUtils.copyFile(new File(DUMMY_SEARCH_HOME, s), new File(SEARCH_HOME, s));
        }
        DefaultConfigRepositoryTestBase.recursiveTouchFuture(SEARCH_HOME);
        
        // Create data folders
        new File(SEARCH_HOME ,"data/config-repository").mkdirs();

        resourceManager = new AutoRefreshResourceManager();
        resourceManager.setAppCacheManager(appCacheManager);
        // Ensure files are checked for freshness at every access
        resourceManager.setCheckingInterval(-1);
        
        configRepository = new WaitConfigRepository();
        configRepository.setAppCacheManager(appCacheManager);
        configRepository.setResourceManager(resourceManager);
        configRepository.setFnConfigParser(new DefaultFacetedNavigationConfigParser());
        configRepository.setSearchHome(SEARCH_HOME);
        configRepository.setCacheTtlSeconds(0);
        configRepository.setAutowireCapableBeanFactory(autowireCapableBeanFactory);
        configRepository.setFileService(fileService);
        
    }
    
    /**
     * Writes content to a file and touch it 1s in the
     * future
     * @param file
     * @param data
     */
    public static void writeAndTouchFuture(File file, String data) throws IOException {
        FileUtils.writeStringToFile(file, data);
        touchFuture(file);
    }
    
    /**
     * Touches a file in the future (current time
     * + 1 second). This is to be able to run tests
     * on filesystems where the time resolution is 1s
     * like ext3 and ext4
     * @param f
     */
    public static void touchFuture(File f) {
        long ts = Math.max(System.currentTimeMillis(), f.lastModified()) + 1000 * modifiedTimes.incrementAndGet();
        f.setLastModified(ts);
    }
    
    /**
     * Recursively touches files to update their timestamp
     * to 1s in the future.
     */
    public static void recursiveTouchFuture(File parent) {
        for (File f: parent.listFiles()) {
            if (f.isDirectory()) {
                recursiveTouchFuture(f);
            } else {
                touchFuture(f);
            }
        }
    }
    
    /**
     * Wrapper for {@link DefaultConfigRepository} that will wait
     * a bit before calling the wrapped method so that the cache expires.
     */
    protected static class WaitConfigRepository extends DefaultConfigRepository {
        @Override
        public Collection getCollection(String collectionId) {
            try { Thread.sleep(10); }
            catch (InterruptedException ie) { }
            
            return super.getCollection(collectionId);
        }
    }
}

