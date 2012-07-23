package com.funnelback.publicui.test.search.service.config;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.CacheManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.search.service.resource.AutoRefreshResourceManager;
import com.funnelback.publicui.xml.StaxStreamFacetedNavigationConfigParser;

public abstract class DefaultConfigRepositoryTestBase {

	private File DUMMY_SEARCH_HOME = new File("src/test/resources/dummy-search_home");
	protected File SEARCH_HOME = new File("target/test-output/config-repository");
	protected File TEST_DIR = new File(SEARCH_HOME, "conf/config-repository");
	
	protected WaitConfigRepository configRepository;
	private AutoRefreshResourceManager resourceManager;
	
	@Autowired
	private CacheManager appCacheManager;
	

	/**
	 * Create fake SEARCH_HOME in target/
	 * as we'll be fiddling with files
	 */
	@Before
	public void before() throws IOException {
		PropertyConfigurator.configure(DefaultValues.DEFAULT_LOG4J_CONSOLE_DEBUG_PROPERTIES);
		
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
		configRepository.setFnConfigParser(new StaxStreamFacetedNavigationConfigParser());
		configRepository.setSearchHome(SEARCH_HOME);
		configRepository.setCacheTtlSeconds(0);
		
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
	 * like ext3
	 * @param f
	 */
	public static void touchFuture(File f) {
		long ts = System.currentTimeMillis() + 2000;
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

