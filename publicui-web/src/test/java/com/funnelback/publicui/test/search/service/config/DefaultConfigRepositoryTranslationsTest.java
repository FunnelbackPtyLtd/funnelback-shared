package com.funnelback.publicui.test.search.service.config;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

import net.sf.ehcache.CacheManager;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.search.service.resource.AutoRefreshResourceManager;
import com.funnelback.publicui.xml.StaxStreamFacetedNavigationConfigParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultConfigRepositoryTranslationsTest {

	private File DUMMY_SEARCH_HOME = new File("src/test/resources/config-i18n");
	private File SEARCH_HOME = new File("target/test-output/config-i18n");
	private File TEST_DIR = new File(SEARCH_HOME, "conf/i18n-collection");
	
	private DefaultConfigRepository configRepository;
	private AutoRefreshResourceManager resourceManager;
	
	@Autowired
	private CacheManager appCacheManager;
	

	/**
	 * Create fake SEARCH_HOME in target/
	 * as we'll be fiddling with files
	 */
	@Before
	public void before() throws IOException {
		FileUtils.deleteDirectory(SEARCH_HOME);
		TEST_DIR.mkdirs();
		FileUtils.copyDirectory(DUMMY_SEARCH_HOME, SEARCH_HOME);
		DefaultConfigRepositoryTestBase.recursiveTouchFuture(SEARCH_HOME);
		
		resourceManager = new AutoRefreshResourceManager();
		resourceManager.setAppCacheManager(appCacheManager);
		// Ensure files are checked for freshness at every access
		resourceManager.setCheckingInterval(-1);
		
		configRepository = new DefaultConfigRepository();
		configRepository.setAppCacheManager(appCacheManager);
		configRepository.setResourceManager(resourceManager);
		configRepository.setFnConfigParser(new StaxStreamFacetedNavigationConfigParser());
		configRepository.setSearchHome(SEARCH_HOME);
		configRepository.setCacheTtlSeconds(0);
		
	}
	
	@Test
	public void testBasic() {
		Map<String, String> translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		
		Assert.assertNull(translations.get("non-existent"));
		Assert.assertEquals("Collection+locale should take precedence", "Search (Collection fr_BE)", translations.get("search"));
		Assert.assertEquals("Global", translations.get("global"));
		Assert.assertEquals("Global fr", translations.get("global.fr"));
		Assert.assertEquals("Global fr_BE", translations.get("global.fr_BE"));
		Assert.assertEquals("Collection", translations.get("collection"));
		Assert.assertEquals("Collection fr", translations.get("collection.fr"));
		Assert.assertEquals("Collection fr_BE", translations.get("collection.fr_BE"));		
	}
	

	@Test
	public void testReloading() throws IOException {
		Map<String, String> translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertEquals("Search (Collection fr_BE)", translations.get("search"));
		
		DefaultConfigRepositoryTestBase.writeAndTouchFuture(new File(TEST_DIR, "ui.fr_BE.cfg"), "search=New translation");
		translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertEquals("New translation", translations.get("search"));
		

	}

	@Test
	public void testNonAscii() {
		Map<String, String> translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		
		Assert.assertEquals("\u4E2D\u56FD", translations.get("china"));
		Assert.assertEquals("Accentu\u00E9", translations.get("french"));
		Assert.assertEquals("\u010Cesk\u00E1 republika", translations.get("czech republic"));
	}
	
	/**
	 * Delete all files one by one, from the more specific to the less specific.
	 * Check for each file that the previous one in the hierarchy takes precedence
	 */
	@Test
	public void testHierarchy() {
		Map<String, String> translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertEquals("Search (Collection fr_BE)", translations.get("search"));
		
		new File(TEST_DIR, "ui.fr_BE.cfg").delete();
		translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertEquals("Search (Global fr_BE)", translations.get("search"));

		new File(SEARCH_HOME, "conf/ui.fr_BE.cfg").delete();
		translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertEquals("Search (Collection fr)", translations.get("search"));

		new File(TEST_DIR, "ui.fr.cfg").delete();
		translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertEquals("Search (Global fr)", translations.get("search"));

		new File(SEARCH_HOME, "conf/ui.fr.cfg").delete();
		translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertEquals("Search (Collection)", translations.get("search"));

		new File(TEST_DIR, "ui.cfg").delete();
		translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertEquals("Search (Global)", translations.get("search"));
		
		new File(SEARCH_HOME, "conf/ui.cfg").delete();
		translations = configRepository.getTranslations("i18n-collection", new Locale("fr", "BE"));
		Assert.assertNull(translations.get("search"));
	}
	
}

