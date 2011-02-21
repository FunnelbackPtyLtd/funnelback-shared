package com.funnelback.publicui.test.search.service.log;

import java.io.File;
import java.io.IOException;
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

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.service.log.LocalLogService;

public class LocalLogServiceTests {

	private static final String COLLECTION_NAME = "log-service";
	
	private static final File TEST_OUT_ROOT = new File("target" + File.separator
			+ "test-output" + File.separator + COLLECTION_NAME); 
	
	private static final File TEST_IN_ROOT = new File("src" + File.separator
			+ "test" + File.separator + "resources" + File.separator + COLLECTION_NAME);
	
	private File contextualNavLogFile = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_DATA
			+ File.separator + COLLECTION_NAME
			+ File.separator + DefaultValues.VIEW_LIVE
			+ File.separator + DefaultValues.FOLDER_LOG,
			Files.Log.CONTEXTUAL_NAVIGATION_LOG_FILENAME);
	
	private LocalLogService logService;
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		TEST_OUT_ROOT.mkdirs();
		FileUtils.cleanDirectory(TEST_OUT_ROOT);
		FileUtils.copyDirectory(TEST_IN_ROOT, TEST_OUT_ROOT);
	}
	
	@Before
	public void before() {
		logService = new LocalLogService();
		if (contextualNavLogFile.exists()) {
			Assert.assertTrue(contextualNavLogFile.delete());
		}
	}
	
	@Test
	public void testLogContextualNavigation() throws Exception {
		Date now = new Date();
		
		NoOptionsConfig config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME);
		Collection c = new Collection(COLLECTION_NAME, config);
		Profile p = new Profile("profile");
		
		ContextualNavigationLog log = new ContextualNavigationLog(
				now,
				c,
				p,
				"userId",
				"cluster",
				Arrays.asList(new String[] {"previousClusters"}));
		
			
		logService.logContextualNavigation(log);
		
		Assert.assertTrue("Log file should have been created", contextualNavLogFile.exists());
		
		String actual = FileUtils.readFileToString(contextualNavLogFile).replace("\r", "");
		String expected = FileUtils.readFileToString(new File(TEST_IN_ROOT, "expected-cn-log.xml")).replace("\r", "");
		
		expected = expected.replace("{DATE}", ContextualNavigationLog.DATE_FORMAT.format(now));
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testThreadSafe() throws Exception {
		NoOptionsConfig config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME);
		final Collection c = new Collection(COLLECTION_NAME, config);
		final Profile p = new Profile("profile");
		final ContextualNavigationLog log = new ContextualNavigationLog(
				new Date(),
				c,
				p,
				"userId",
				"cluster",
				Arrays.asList(new String[] {"previousClusters"}));
				
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
				Arrays.asList(new String[] {"previousClusters"}));
		
		logService.logContextualNavigation(log);
		
		Assert.assertFalse("Log file should not have been created", contextualNavLogFile.exists());
	}
	
	@Test
	public void testLogFileWithTrailingNewLine() throws Exception {
		Date now = new Date();
		
		NoOptionsConfig config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME);
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
				Arrays.asList(new String[] {"previousClusters"}));
		
			
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
				Arrays.asList(new String[] {"previousClusters"}));
		
			
		logService.logContextualNavigation(log);

		String actual = FileUtils.readFileToString(contextualNavLogFile).replace("\r", "");
		String expected = FileUtils.readFileToString(new File(TEST_IN_ROOT, "invalid-log.xml")).replace("\r", "");
		
		Assert.assertEquals("Invalid log shouldn't have been updated", expected, actual);
	}
	
}
