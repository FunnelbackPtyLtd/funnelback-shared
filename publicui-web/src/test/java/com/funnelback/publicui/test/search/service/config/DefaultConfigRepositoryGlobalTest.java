package com.funnelback.publicui.test.search.service.config;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository.GlobalConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultConfigRepositoryGlobalTest extends DefaultConfigRepositoryTestBase {

	@Test
	public void testAllCollections() throws Exception {
		List<String> ids = configRepository.getAllCollectionIds();
		Assert.assertEquals(1, ids.size());
		Assert.assertEquals("config-repository", ids.get(0));
		
		List<Collection> all = configRepository.getAllCollections();
		Assert.assertEquals(1, all.size());
		Assert.assertEquals("config-repository", all.get(0).getId());
		Assert.assertEquals("Test config repository", all.get(0).getConfiguration().getServiceName());
		
		// Create new collection
		new File(SEARCH_HOME, "conf/config-repository-2").mkdirs();
		new File(SEARCH_HOME, "data/config-repository-2").mkdirs();
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/config-repository-2/collection.cfg"),
				"service_name=Second collection");

		sleep(5);
		ids = configRepository.getAllCollectionIds();
		Assert.assertEquals(2, ids.size());
		Assert.assertTrue(ids.contains("config-repository"));
		Assert.assertTrue(ids.contains("config-repository-2"));
		
		all = configRepository.getAllCollections();
		Assert.assertEquals(2, all.size());
		
		Collection c = (Collection) CollectionUtils.find(all, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return ((Collection) o).getId().equals("config-repository");
			}
		});
		
		Assert.assertNotNull(c);
		Assert.assertEquals("Test config repository", c.getConfiguration().getServiceName());
		
		c = (Collection) CollectionUtils.find(all, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return ((Collection) o).getId().equals("config-repository-2");
			}
		});
		
		Assert.assertNotNull(c);
		Assert.assertEquals("Second collection", c.getConfiguration().getServiceName());
		
		// And delete the first one
		FileUtils.deleteDirectory(TEST_DIR);
		FileUtils.deleteDirectory(new File(SEARCH_HOME, "data/config-repository"));

		sleep(5);
		ids = configRepository.getAllCollectionIds();
		Assert.assertEquals(1, ids.size());
		Assert.assertEquals("config-repository-2", ids.get(0));
		
		all = configRepository.getAllCollections();
		Assert.assertEquals(1, all.size());
		Assert.assertEquals("config-repository-2", all.get(0).getId());
		Assert.assertEquals("Second collection", all.get(0).getConfiguration().getServiceName());
	}
	
	@Test
	public void getGlobalConfiguration() throws IOException {
		Config c = configRepository.getGlobalConfiguration();
		
		Assert.assertEquals("Global value", c.value("global-key"));
		Assert.assertNull(c.value("new-key"));
		Assert.assertNull(c.value("local-key"));
		
		// Update the default config
		String content = FileUtils.readFileToString(new File(SEARCH_HOME, "conf/global.cfg.default"));
		content += "\n"+"new-key=New value";
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/global.cfg.default"), content);
		
		sleep(5);
		c = configRepository.getGlobalConfiguration();
		Assert.assertEquals("Global value", c.value("global-key"));
		Assert.assertEquals("New value", c.value("new-key"));
		Assert.assertNull(c.value("local-key"));
		
		// Create a local customised global config
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/global.cfg"), "local-key=Local value");
		sleep(5);
		c = configRepository.getGlobalConfiguration();
		Assert.assertEquals("Global value", c.value("global-key"));
		Assert.assertEquals("New value", c.value("new-key"));
		Assert.assertEquals("Local value", c.value("local-key"));
		
		// Change value in custom file
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/global.cfg"), "local-key=New local value");
		sleep(5);
		c = configRepository.getGlobalConfiguration();
		Assert.assertEquals("Global value", c.value("global-key"));
		Assert.assertEquals("New value", c.value("new-key"));
		Assert.assertEquals("New local value", c.value("local-key"));
		
		// Delete value in custom file
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/global.cfg"), "another-key=Another value");
		sleep(5);
		c = configRepository.getGlobalConfiguration();
		Assert.assertEquals("Global value", c.value("global-key"));
		Assert.assertEquals("New value", c.value("new-key"));
		Assert.assertNull(c.value("local-key"));
	}
	
	@Test
	public void testGetGlobalConfigurationFile() throws IOException {
		Map<String, String> data = configRepository.getGlobalConfigurationFile(GlobalConfiguration.DNSAliases);
		Assert.assertNull(data);
		
		// Create file
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/"+Files.DNS_ALIASES_FILENAME), "key=value");
		sleep(5);
		data = configRepository.getGlobalConfigurationFile(GlobalConfiguration.DNSAliases);
		Assert.assertEquals("value", data.get("key"));
		Assert.assertNull(data.get("second-key"));
		
		// Add value
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/"+Files.DNS_ALIASES_FILENAME), "key=value\nsecond-key=Second value");
		sleep(5);
		data = configRepository.getGlobalConfigurationFile(GlobalConfiguration.DNSAliases);
		Assert.assertEquals("value", data.get("key"));
		Assert.assertEquals("Second value", data.get("second-key"));
		
		// Delete file
		new File(SEARCH_HOME, "conf/"+Files.DNS_ALIASES_FILENAME).delete();
		sleep(5);
		data = configRepository.getGlobalConfigurationFile(GlobalConfiguration.DNSAliases);
		Assert.assertNull(data);		
	}
	
	@Test
	public void testGetExecutablePath() throws IOException {
		// No executables.cfg
		try {
			configRepository.getExecutablePath("perl");
			Assert.fail("Should have thrown an Exception");
		} catch (RuntimeException ne) {
			Assert.assertEquals(ne.getCause().getClass(), NullPointerException.class);
		}
		
		// Create file
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/"+Files.EXECUTABLES_CONFIG_FILENAME), "java=java.exe\nperl=/usr/bin/perl");
		sleep(5);
		Assert.assertEquals("/usr/bin/perl", configRepository.getExecutablePath("perl"));
		Assert.assertEquals("java.exe", configRepository.getExecutablePath("java"));
		Assert.assertNull(configRepository.getExecutablePath("python"));
		
		// Change value
		FileUtils.writeStringToFile(new File(SEARCH_HOME, "conf/"+Files.EXECUTABLES_CONFIG_FILENAME), "java=jvm.dll\nperl=/usr/bin/perl");
		sleep(5);
		Assert.assertEquals("/usr/bin/perl", configRepository.getExecutablePath("perl"));
		Assert.assertEquals("jvm.dll", configRepository.getExecutablePath("java"));
		Assert.assertNull(configRepository.getExecutablePath("python"));

	}
	
	@Test
	public void testGetForms() throws IOException {
		Assert.assertEquals(0, configRepository.getForms("config-repository", "_default").length);
		Assert.assertEquals(0, configRepository.getForms("config-repository", "_default_preview").length);
		
		// Create profile folders
		new File(TEST_DIR, "_default").mkdirs();
		new File(TEST_DIR, "_default_preview").mkdirs();
		Assert.assertEquals(0, configRepository.getForms("config-repository", "_default").length);
		Assert.assertEquals(0, configRepository.getForms("config-repository", "_default_preview").length);
		
		// Create one form + non-form files
		new File(TEST_DIR, "_default/simple.ftl").createNewFile();
		new File(TEST_DIR, "_default/non-form.file").createNewFile();
		new File(TEST_DIR, "_default/old.form").createNewFile();
		Assert.assertEquals(1, configRepository.getForms("config-repository", "_default").length);
		Assert.assertEquals("simple", configRepository.getForms("config-repository", "_default")[0]);
		Assert.assertEquals(0, configRepository.getForms("config-repository", "_default_preview").length);

		// Create second form
		new File(TEST_DIR, "_default/form.ftl").createNewFile();
		Assert.assertEquals(2, configRepository.getForms("config-repository", "_default").length);
		Assert.assertTrue(ArrayUtils.contains(configRepository.getForms("config-repository", "_default"), "simple"));
		Assert.assertTrue(ArrayUtils.contains(configRepository.getForms("config-repository", "_default"), "form"));
		Assert.assertEquals(0, configRepository.getForms("config-repository", "_default_preview").length);
		
		// Delete first form
		new File(TEST_DIR, "_default/simple.ftl").delete();
		Assert.assertEquals(1, configRepository.getForms("config-repository", "_default").length);
		Assert.assertEquals("form", configRepository.getForms("config-repository", "_default")[0]);
		Assert.assertEquals(0, configRepository.getForms("config-repository", "_default_preview").length);

	}
	
	protected void sleep(long ms) {
		try {Thread.sleep(ms);}
		catch (InterruptedException ie) { }
	}
}

