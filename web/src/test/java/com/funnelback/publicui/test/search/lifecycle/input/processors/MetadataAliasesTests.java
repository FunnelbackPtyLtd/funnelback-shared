package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MetadataAliases;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MetadataAliasesTests {

	private MetadataAliases processor;
	private SearchTransaction st;
	
	@Before
	public void before() {
		processor = new MetadataAliases();
		st = new SearchTransaction(new SearchQuestion(), null);
		
	}

	@Test
	public void testLink() throws InputProcessorException {
		st.getQuestion().setQuery("abc link:http://www.funnelback.com def link:file:///file.txt link :me");
		processor.process(st, null);
		Assert.assertEquals("abc h:http://www.funnelback.com def h:file:///file.txt link :me", st.getQuestion().getQuery());
	}
	
	@Test
	public void testSite() throws InputProcessorException {
		st.getQuestion().setQuery("abc site:http://www.funnelback.com def site:file:///file.txt site :me");
		processor.process(st, null);
		Assert.assertEquals("abc u:http://www.funnelback.com def u:file:///file.txt site :me", st.getQuestion().getQuery());
	}
	
	@Test
	public void testFiletype() throws InputProcessorException {
		st.getQuestion().setQuery("abc filetype:pdf def doc filetype:jpeg filetype :test file type :me");
		processor.process(st, null);
		Assert.assertEquals("abc f:pdf def doc f:jpeg filetype :test file type :me", st.getQuestion().getQuery());
	}
	
	@Test
	public void testAllInUrl() throws InputProcessorException {
		st.getQuestion().setQuery("abc allinurl:test/url def allinurl:test/again/url.ext allin url :test allinurl :me");
		processor.process(st, null);
		Assert.assertEquals("abc v:test/url def v:test/again/url.ext allin url :test allinurl :me", st.getQuestion().getQuery());
	}
	
	@Test
	public void testInvalidParameters() {
		try {
			processor.process(null, null);
			processor.process(new SearchTransaction(null, null), null);
			processor.process(new SearchTransaction(new SearchQuestion(), null), null);
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testEmptyQuery() {
		st.getQuestion().setQuery("");
		try {
			processor.process(st, null);
		} catch (Exception e) {
			Assert.fail();
		}
		
		Assert.assertEquals("", st.getQuestion().getQuery());
	}
	
	@Test
	public void testNoOperatorShouldntAffectQuery() throws InputProcessorException {
		String expected = "There is no operator in this query";
		st.getQuestion().setQuery(expected);
		processor.process(st, null);
		Assert.assertEquals(expected, st.getQuestion().getQuery());
	}
	
	
}
