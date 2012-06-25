package com.funnelback.publicui.test.search.lifecycle.input.processors.userkeys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.MoodleMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MoodleMapperTest {

	private static Collection c;

	@BeforeClass
	public static void before() throws IOException {
		c = new Collection("moodle", new NoOptionsConfig(new File(
				"src/test/resources/dummy-search_home"), "moodle"));
		PropertyConfigurator.configure(DefaultValues.DEFAULT_LOG4J_CONSOLE_DEBUG_PROPERTIES);
	}

	@Test
	public void testNoHeader()  {
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);
		SearchTransaction st = new SearchTransaction(question, null);
		MoodleMapper mm = new MoodleMapper();
		Assert.assertEquals("", mm.getUserKeys(st).get(0));
	}
	
	@Test
	public void testWrongHeader()
	{
		MoodleMapper mm = new MoodleMapper();
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);
		question.getRawInputParameters().put(
				MoodleMapper.MOODLE_PARAMETER_NAME, new String[] { "Mad user" });
		SearchTransaction st = new SearchTransaction(question, null);
		Assert.assertEquals(
				"",
				mm.getUserKeys(st).get(0));
	}
	
	@Test
	public void testHeader() {
		MoodleMapper mm = new MoodleMapper();
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);
		question.getRawInputParameters().put(
				MoodleMapper.MOODLE_PARAMETER_NAME, new String[] { "user1" });
		SearchTransaction st = new SearchTransaction(question, null);
		Assert.assertEquals(
				new ArrayList<String>(
						Arrays.asList("C1_R5_C2_R5_C3_R5_C5_R5_C2_M10_R3_C2_M22_R3_C3_M42_R3_")),
				mm.getUserKeys(st));

		question.getRawInputParameters().put(
				MoodleMapper.MOODLE_PARAMETER_NAME, new String[] { "user2" });
		Assert.assertEquals(
				new ArrayList<String>(Arrays
						.asList("C1_R5_C3_R5_C4_R5_C5_R5_C4_M39_R3_")), mm
						.getUserKeys(st));

		question.getRawInputParameters().put(
				MoodleMapper.MOODLE_PARAMETER_NAME, new String[] { "user3" });
		Assert.assertEquals(
				new ArrayList<String>(Arrays
						.asList("C1_R3_C2_R3_C5_R3_C3_R5_C2_M9_R3_")), mm
						.getUserKeys(st));

		question.getRawInputParameters().put(
				MoodleMapper.MOODLE_PARAMETER_NAME, new String[] { "user4" });
		Assert.assertEquals(new ArrayList<String>(Arrays
				.asList("C1_R3_C3_R3_C4_R3_C5_R4_")), mm.getUserKeys(st));
	}
	
	@Test
	public void testFormatUserkeys()
	{
		MoodleMapper mm = new MoodleMapper();
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);
		question.getRawInputParameters().put(
				MoodleMapper.MOODLE_PARAMETER_NAME, new String[] { "user1" });
		SearchTransaction st = new SearchTransaction(question, null);
		Assert.assertTrue(mm.getUserKeys(st).get(0).contains("C"));
		Assert.assertTrue(mm.getUserKeys(st).get(0).contains("R"));
	}
}
