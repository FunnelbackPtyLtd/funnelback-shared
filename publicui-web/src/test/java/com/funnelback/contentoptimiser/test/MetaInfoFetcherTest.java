package com.funnelback.contentoptimiser.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.contentoptimiser.MultipleConfigReader;
import com.funnelback.contentoptimiser.fetchers.impl.MetaInfoFetcher;
import com.funnelback.contentoptimiser.processors.impl.MetaInfo;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

public class MetaInfoFetcherTest {

	private final File searchHome = new File("src/test/resources/dummy-search_home");
	private MultipleConfigReader<MetaInfo> reader;
	private Collection c;
	private Set<String> mustExist;
	private Profile profile;

	@Before
	public void setUp() throws FileNotFoundException {
		c = new Collection("data-repository",
				new NoOptionsConfig(searchHome, "data-repository")
					.setValue("collection_root",
							new File("src/test/resources/dummy-search_home/data/data-repository/").getAbsolutePath() ));
		
		c.getConfiguration().setValue(Keys.QUERY_PROCESSOR_OPTIONS, "-daat -wmeta k 0.1 -wmeta t 0.1");
		profile = mock(Profile.class);
		when(profile.getPadreOpts()).thenReturn("-stem2 -wmeta k 0.2 -wmeta J 0.3");
	
		reader = mock(MultipleConfigReader.class);
		mustExist = new HashSet<String>();
		mustExist.add(searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml.default");
	}
	
	@Test public void testRankerOptionsCorrect() {
		String profileName = "profile";
		c.getProfiles().put(profileName, profile);
		MetaInfoFetcher f = new MetaInfoFetcher(c,profileName);
		Assert.assertEquals(0.2,f.getRankerOptions().getMetaWeight("k"));
		Assert.assertEquals(0.1,f.getRankerOptions().getMetaWeight("t"));
		Assert.assertEquals(0.3,f.getRankerOptions().getMetaWeight("J"));
	}
	
	@Test
	public void testMetaInfoWithProfile() throws FileNotFoundException {
		String profileName = "profile";
		c.getProfiles().put(profileName, profile);
		MetaInfoFetcher f = new MetaInfoFetcher(c,profileName);
		f.setConfigReader(reader);
		f.fetch(searchHome, profileName);

		List<String> fileNames = Arrays.asList(new String[] {
				searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml.default",
				searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml",		
				new File (searchHome + File.separator+ "conf" + File.separator  + "data-repository" + File.separator + "meta-names.xml").getAbsolutePath(),
				new File (searchHome + File.separator+ "conf" + File.separator  + "data-repository" + File.separator + profileName + File.separator + "meta-names.xml").getAbsolutePath(),
				});
		verify(reader).read(fileNames , mustExist);
	}
	
	@Test
	public void testMetaInfoWithoutProfile() throws FileNotFoundException {
		String profileName = "_default";
		c.getProfiles().put(profileName, profile);
		MetaInfoFetcher f = new MetaInfoFetcher(c,profileName);
		f.setConfigReader(reader);
		f.fetch(searchHome, null);

		List<String> fileNames = Arrays.asList(new String[] {
				searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml.default",
				searchHome + File.separator + DefaultValues.FOLDER_CONF + File.separator + "meta-names.xml",		
				new File (searchHome + File.separator+ "conf" + File.separator  + "data-repository" + File.separator + "meta-names.xml").getAbsolutePath(),
				new File (searchHome + File.separator+ "conf" + File.separator  + "data-repository" + File.separator + profileName + File.separator + "meta-names.xml").getAbsolutePath(),
				});
		verify(reader).read(fileNames , mustExist);
	}

}
