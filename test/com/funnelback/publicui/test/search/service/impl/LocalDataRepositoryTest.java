package com.funnelback.publicui.test.search.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.data.LocalDataRepository;

public class LocalDataRepositoryTest {

	private LocalDataRepository dataRepository;
	private Collection collection;
	private File collectionRoot;
	
	@Before
	public void before() throws FileNotFoundException, EnvironmentVariableException {
		collection = new Collection("data-repository", new NoOptionsConfig("data-repository"));
		collectionRoot = new File("test_data/dummy-search_home/data/data-repository/");
		collection.getConfiguration().setValue(Keys.COLLECTION_ROOT, collectionRoot.getAbsolutePath());
		dataRepository = new LocalDataRepository();
	}
	
	@Test
	public void testGetCachedDocumentLive() throws FileNotFoundException, IOException {
		String content = dataRepository.getCachedDocument(collection, "cached-doc.live.txt");
		
		Assert.assertNotNull(content);
		Assert.assertEquals(FileUtils.readFileToString(
						new File(collectionRoot, DefaultValues.VIEW_LIVE
								+ File.separator + DefaultValues.VIEW_FOLDER_DATA
								+ File.separator + "cached-doc.live.txt")), content);
	}
	
	@Test
	public void testGetCachedDocumentOffline() throws FileNotFoundException, IOException {
		String content = dataRepository.getCachedDocument(collection, "cached-doc.offline.txt");
		
		Assert.assertNotNull(content);
		Assert.assertEquals(FileUtils.readFileToString(
						new File(collectionRoot, DefaultValues.VIEW_OFFLINE
								+ File.separator + DefaultValues.VIEW_FOLDER_DATA
								+ File.separator + "cached-doc.offline.txt")), content);
	}
	
	@Test
	public void testGetCachedDocumentSecondaryData() throws FileNotFoundException, IOException {
		String content = dataRepository.getCachedDocument(collection, "cached-doc.secondary-data.txt");
		
		Assert.assertNotNull(content);
		Assert.assertEquals(FileUtils.readFileToString(
						new File(collectionRoot, DefaultValues.VIEW_LIVE
								+ File.separator + DefaultValues.VIEW_FOLDER_SECONDARY_DATA
								+ File.separator + "cached-doc.secondary-data.txt")), content);
	}
	
}
