package com.funnelback.publicui.test.search.web.views.freemarker;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.web.views.freemarker.FallbackFreeMarkerViewResolver;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/test/resources/spring/freemarkerViewResolver.xml")
public class FallbackFreeMarkerViewResolverTest {

	@Autowired
	private File searchHome;
	
	private File confDir;
	
	@Autowired
	private FallbackFreeMarkerViewResolver resolver;
	
	@Autowired
	private FreeMarkerConfigurer config;
	
	@Before
	public void before() throws IOException {
		// Prepare a collection configuration folder
		FileUtils.deleteDirectory(searchHome);
		searchHome.mkdirs();
		
		confDir = new File(searchHome, DefaultValues.FOLDER_CONF + "/dummy");
		FileUtils.deleteDirectory(confDir);
		confDir.mkdirs();
		new File(confDir, DefaultValues.DEFAULT_PROFILE).mkdirs();
	}
	
	@Test
	public void testUnknownView() throws Exception {
		Assert.assertNull(resolver.resolveViewName("unknown", Locale.getDefault()));
	}
	
	@Test
	public void testProfileView() throws Exception {
		new File(confDir+ "/" + DefaultValues.DEFAULT_PROFILE, "simple.ftl").createNewFile();
		FreeMarkerView v = (FreeMarkerView) resolver.resolveViewName("conf/dummy/_default/simple", Locale.getDefault());
		Assert.assertNotNull(v);
		Assert.assertEquals("conf/dummy/_default/simple.ftl", v.getUrl());
	}

	@Test
	public void testCollectionView() throws Exception {
		new File(confDir, "simple.ftl").createNewFile();
		Assert.assertFalse(new File(confDir+ "/" + DefaultValues.DEFAULT_PROFILE, "simple.ftl").exists());
		FreeMarkerView v = (FreeMarkerView) resolver.resolveViewName("conf/dummy/_default/simple", Locale.getDefault());
		Assert.assertNotNull(v);
		Assert.assertEquals("conf/dummy/_default/simple.ftl", v.getUrl());
	}

	@Test
	public void testGlobalConfView() throws Exception {
		new File(searchHome, "conf/simple.ftl").createNewFile();
		Assert.assertFalse(new File(confDir, "simple.ftl").exists());
		Assert.assertFalse(new File(confDir+ "/" + DefaultValues.DEFAULT_PROFILE, "simple.ftl").exists());
		FreeMarkerView v = (FreeMarkerView) resolver.resolveViewName("conf/dummy/_default/simple", Locale.getDefault());
		Assert.assertNotNull(v);
		Assert.assertEquals("conf/dummy/_default/simple.ftl", v.getUrl());
	}
	

}
