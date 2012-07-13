package com.funnelback.publicui.test.search.service.resource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.service.resource.AutoRefreshResourceManager;
import com.funnelback.publicui.search.service.resource.ResourceParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class AutoRefreshResourceManagerTest {
	
	private static final File TEST_DIR = new File("target/test-output/resource-manager");
	
	@Autowired
	protected CacheManager appCacheManager;
	
	private Cache cache;
	private AutoRefreshResourceManager manager;

	@Before
	public void before() throws Exception {
		FileUtils.deleteDirectory(TEST_DIR);
		TEST_DIR.mkdirs();
		FileUtils.copyDirectory(new File("src/test/resources/resource-manager/"), TEST_DIR);
		
		manager = new AutoRefreshResourceManager();
		manager.setAppCacheManager(appCacheManager);
		
		cache = appCacheManager.getCache("localConfigFilesRepository");
	}
	
	@Test
	public void test() throws IOException {
		Assert.assertEquals(0, cache.getSize());
		
		File testFile = new File(TEST_DIR, "test.properties");
		PropertiesResourceParser parser = new PropertiesResourceParser();
		Properties props = manager.load(testFile, parser);
		
		Assert.assertNotNull(props);
		Assert.assertEquals(2, props.size());
		Assert.assertEquals("Test properties file", props.get("title"));
		Assert.assertEquals("42", props.get("value"));
		Assert.assertEquals(1, cache.getSize());
		Assert.assertNotNull(cache.get(testFile.getAbsolutePath()));
		long timestamp = cache.get(testFile.getAbsolutePath()).getLatestOfCreationAndUpdateTime();
		
		// Second retrieval should yield the same object
		// retrieved from the cache
		Properties same = manager.load(testFile, parser);
		Assert.assertEquals(props, same);
		
		// Modifying the properties
		FileUtils.writeStringToFile(testFile,
				"title=Updated title" + System.getProperty("line.separator") +
				"value=42" + System.getProperty("line.separator") +
				"second.value=678" + System.getProperty("line.separator"));
		
		Properties updated = manager.load(testFile, parser);
		Assert.assertNotSame(props, updated);
		Assert.assertEquals(3, updated.size());
		Assert.assertEquals("Updated title", updated.get("title"));
		Assert.assertEquals("42", updated.get("value"));
		Assert.assertEquals("678", updated.get("second.value"));
		Assert.assertEquals(1, cache.getSize());
		Assert.assertNotNull(cache.get(testFile.getAbsolutePath()));
		Assert.assertTrue(cache.get(testFile.getAbsolutePath()).getLatestOfCreationAndUpdateTime() > timestamp);


	}

	private class PropertiesResourceParser implements ResourceParser<Properties> {

		@Override
		public Properties parse(File f) throws IOException {
			Properties props = new Properties();
			
			FileReader reader = new FileReader(f);
			props.load(reader);
			reader.close();
			
			return props;
		}
		
	}
	
}
