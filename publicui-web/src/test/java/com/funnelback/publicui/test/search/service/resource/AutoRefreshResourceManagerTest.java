package com.funnelback.publicui.test.search.service.resource;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.service.resource.AutoRefreshResourceManager;
import com.funnelback.publicui.search.service.resource.impl.PropertiesResource;
import com.funnelback.publicui.test.search.service.config.DefaultConfigRepositoryTestBase;

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
		PropertyConfigurator.configure(DefaultValues.DEFAULT_LOG4J_CONSOLE_DEBUG_PROPERTIES);
		
		FileUtils.deleteDirectory(TEST_DIR);
		TEST_DIR.mkdirs();
		FileUtils.copyDirectory(new File("src/test/resources/resource-manager/"), TEST_DIR);
		
		manager = new AutoRefreshResourceManager();
		manager.setAppCacheManager(appCacheManager);
		// Ensure files are checked for freshness at every access
		manager.setCheckingInterval(-1);
		
		cache = appCacheManager.getCache("localConfigFilesRepository");
		cache.removeAll();
		Assert.assertEquals(0, cache.getSize());
	}
	
	@Test
	public void test() throws IOException {
		File testFile = new File(TEST_DIR, "test.properties");
		PropertiesResource parser = new PropertiesResource(testFile);
		Properties props = manager.load(parser);
		
		Assert.assertNotNull(props);
		Assert.assertEquals(2, props.size());
		Assert.assertEquals("Test properties file", props.get("title"));
		Assert.assertEquals("42", props.get("value"));
		Assert.assertEquals(1, cache.getSize());
		Element elt = cache.get(testFile.getAbsolutePath());
		Assert.assertNotNull(elt);
		long timestamp = elt.getLatestOfCreationAndUpdateTime();
		
		// Wait a bit for the timestamps to be different
		// Need to wait more than 1s for ext3 filesystems
		try { Thread.sleep(1500); }
		catch (InterruptedException ie) { }
		
		// Second retrieval should yield the same object
		// retrieved from the cache
		manager.load(parser);
		Assert.assertEquals(elt,  cache.get(testFile.getAbsolutePath()));
		
		// Modifying the properties
		System.out.println("BEFORE: " + testFile.lastModified());
		DefaultConfigRepositoryTestBase.writeAndTouchFuture(testFile,
				"title=Updated title" + System.getProperty("line.separator") +
				"value=42" + System.getProperty("line.separator") +
				"second.value=678" + System.getProperty("line.separator"));
		System.out.println("AFTER: " + testFile.lastModified());
		
		Properties updated = manager.load(parser);
		Element newElt = cache.get(testFile.getAbsolutePath());
		Assert.assertNotNull(newElt);
		Assert.assertTrue(newElt.getLatestOfCreationAndUpdateTime() + " should be > " + timestamp, newElt.getLatestOfCreationAndUpdateTime() > timestamp);
		Assert.assertEquals(elt,  newElt);
		Assert.assertEquals(3, updated.size());
		Assert.assertEquals("Updated title", updated.get("title"));
		Assert.assertEquals("42", updated.get("value"));
		Assert.assertEquals("678", updated.get("second.value"));
		Assert.assertEquals(1, cache.getSize());
	}
	
	@Test
	public void testDeleteFile() throws IOException {
		File testFile = new File(TEST_DIR, "test.properties");
		PropertiesResource parser = new PropertiesResource(testFile);
		Properties props = manager.load(parser);
		
		Assert.assertNotNull(props);
		Assert.assertEquals(2, props.size());
		Assert.assertEquals("Test properties file", props.get("title"));
		Assert.assertEquals("42", props.get("value"));
		
		testFile.delete();
		
		try { Thread.sleep(25); }
		catch (InterruptedException ie) { }
		
		props = manager.load(parser);
		
		Assert.assertNull(props);
		Assert.assertEquals(0, cache.getSize());
		
		DefaultConfigRepositoryTestBase.writeAndTouchFuture(testFile,
				"title=New title" + System.getProperty("line.separator") +
				"value=42" + System.getProperty("line.separator") +
				"new.value=123" + System.getProperty("line.separator"));
		
		Properties updated = manager.load(parser);
		Assert.assertNotSame(props, updated);
		Assert.assertEquals(3, updated.size());
		Assert.assertEquals("New title", updated.get("title"));
		Assert.assertEquals("42", updated.get("value"));
		Assert.assertEquals("123", updated.get("new.value"));
		Assert.assertEquals(1, cache.getSize());
		Assert.assertNotNull(cache.get(testFile.getAbsolutePath()));
	}
	
	@Test
	public void testNonExistentFile() throws IOException {
		File testFile = new File("non", "existent");
		Assert.assertNull(manager.load(new PropertiesResource(testFile)));
	}
	
	@Test
	public void testCreateFile() throws IOException {
		File sourceFile = new File(TEST_DIR, "test.properties");
		File testFile = new File(TEST_DIR, "test-new.properties");
		
		PropertiesResource parser = new PropertiesResource(testFile);
		Properties props = manager.load(parser);
		
		Assert.assertNull(props);
		Assert.assertEquals(0, cache.getSize());
		
		// Create the file
		FileUtils.copyFile(sourceFile, testFile);
		
		props = manager.load(parser);
		
		Assert.assertNotNull(props);
		Assert.assertEquals(2, props.size());
		Assert.assertEquals("Test properties file", props.get("title"));
		Assert.assertEquals("42", props.get("value"));
		Assert.assertEquals(1, cache.getSize());
		Element elt = cache.get(testFile.getAbsolutePath());
		Assert.assertNotNull(elt);
	}
}
