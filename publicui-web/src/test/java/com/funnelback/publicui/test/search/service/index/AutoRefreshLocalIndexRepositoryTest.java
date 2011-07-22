package com.funnelback.publicui.test.search.service.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.index.AutoRefreshLocalIndexRepository;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class AutoRefreshLocalIndexRepositoryTest {

	@Resource(name="autoRefreshLocalIndexRepository")
	private AutoRefreshLocalIndexRepository indexRepository;
	
	private static final String[][] EXPECTED_BLDINFO = {
		{"version", "version FUNNELBACK_PADRE_10.1.1.110 64MDPLFS (Web/Ent)"},
		{"index_format", "Windows-10.2.DF7"},
		{"indexer_options", "C:\\Data\\dev\\funnelback\\trunk\\funnelback\\bin\\padre-iw\n"
			+ "C:\\Data\\dev\\funnelback\\trunk\\funnelback\\data\\index-repository\\offline\\data\n"
			+ "C:\\Data\\dev\\funnelback\\trunk\\funnelback\\data\\index-repository\\offline\\idx\\index\n"
			+ "-XMFC:\\Data\\dev\\funnelback\\trunk\\funnelback\\conf\\index-repository\\xml.cfg\n"
			+ "-MMFC:\\Data\\dev\\funnelback\\trunk\\funnelback\\conf\\index-repository\\metamap.cfg\n"
			+ "-cleanup"},
		{"idxch_classes", "acdw"},
		{"WORD-POSITIONS", null},
		{"CONTENT_FIELDS", "tcs"},
		{"miebl", "22"},
		{"SORT_SIGNIF", "20"},
		{"facet_item_sepchars", "|"},
		{"VBYTE_COMPRESSION", null},
		{"PACKED_LENS", null},
		{"doctable_fieldwidths", "47 5 3 5 5 4 2 5 5 3 1 1 8 0 0 0"},
		{"Num_docs", "1"},
		{"date_of_most_recent_doc", "0"},
		{"Text_size_in_bytes", "13467"},
		{"Num_postings", "323"},
		{"Longest_postings_list", "56 bytes"},
		{"CHAMBER_HWM", "13467"},
		{"link_onscale", "1.#INF"},
		{"link_offscale", "1.#INF"},
		{"Total content word occurrences", "89"},
		{"Average document length", "89 content words"}
	};
	
	@Before
	public void before() throws FileNotFoundException, EnvironmentVariableException {
		MockConfigRepository configRepository = new MockConfigRepository();
		configRepository.addCollection(
				new Collection("index-repository",
						new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "index-repository")
							.setValue("collection_root",
									new File("src/test/resources/dummy-search_home/data/index-repository/").getAbsolutePath() )));
		indexRepository.setConfigRepository(configRepository);
	}
	
	@Test
	public void testGetLastUpdated() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2011);
		c.set(Calendar.MONTH, Calendar.JULY);
		c.set(Calendar.DAY_OF_MONTH, 19);
		c.set(Calendar.HOUR_OF_DAY, 17);
		c.set(Calendar.MINUTE, 31);
		c.set(Calendar.SECOND, 20);
		c.set(Calendar.MILLISECOND, 0);
		
		Assert.assertEquals(c.getTime(), indexRepository.getLastUpdated("index-repository"));
	}
	
	@Test
	public void testFetBuildInfoValue() {
		for (String[] expected: EXPECTED_BLDINFO) {
			Assert.assertEquals("Build info value for '" + expected[0] + "'",
					expected[1],
					indexRepository.getBuildInfoValue("index-repository", expected[0]));
		}
	}
	
}
