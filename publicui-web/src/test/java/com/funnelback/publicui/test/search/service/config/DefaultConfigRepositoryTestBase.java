package com.funnelback.publicui.test.search.service.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.search.service.resource.AutoRefreshResourceManager;

public abstract class DefaultConfigRepositoryTestBase {

	private File DUMMY_SEARCH_HOME = new File("src/test/resources/dummy-search_home");
	protected File SEARCH_HOME = new File("target/test-output/config-repository");
	protected File TEST_DIR = new File(SEARCH_HOME, "conf/config-repository");
	
	@Autowired
	protected DefaultConfigRepository configRepository;
	
	@Autowired
	private AutoRefreshResourceManager resourceManager;
	

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
		DefaultConfigRepositoryTestBase.recursiveTouch(SEARCH_HOME);
		
		// Create data folders
		new File(SEARCH_HOME ,"data/config-repository").mkdirs();
		
		configRepository.setSearchHome(SEARCH_HOME);
		configRepository.setCacheTtlSeconds(0);
		// Disable cache on resource manager
		resourceManager.setCheckingInterval(0);
		
	}
	
	public static void sleep() {
		try {Thread.sleep(5);}
		catch (InterruptedException ie) { }
	}
	
	/**
	 * Recursively touches files to update their timestamp
	 */
	public static void recursiveTouch(File parent) {
		for (File f: parent.listFiles()) {
			if (f.isDirectory()) {
				recursiveTouch(f);
			} else {
				f.setLastModified(System.currentTimeMillis());
			}
		}
	}
}

