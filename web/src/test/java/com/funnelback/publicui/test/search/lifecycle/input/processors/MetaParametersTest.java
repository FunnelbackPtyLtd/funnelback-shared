package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MetaParameters;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MetaParametersTest {
	
	@Test
	public void testMissingData() throws InputProcessorException {
		MetaParameters processor = new MetaParameters();
		
		// No transaction
		processor.process(null, null);
		
		// No question
		processor.process(new SearchTransaction(null, null), null);
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		processor.process(new SearchTransaction(question, null), null);		
	}
	
	@Test
	public void testNoMetaParameters() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MetaParameters processor = new MetaParameters();
		processor.process(st, request);
		
		Assert.assertEquals(0, st.getQuestion().getMetaParameters().size());
	}
	
	@Test
	public void testNoParameterValue() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_a", (String) null);
		request.addParameter("meta_c_or", "");
		request.addParameter("meta_X_and", (String[]) null);
		request.addParameter("query_phrase", (String) null);
		request.addParameter("query_and", "");
		request.addParameter("query_or", (String[]) null);
		request.addParameter("unrelated", (String) null);
		
		MetaParameters processor = new MetaParameters();
		processor.process(st, request);
		
		Assert.assertEquals(0, st.getQuestion().getMetaParameters().size());
	}
	
	@Test
	public void testMetaMultipleWords() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_a", "simple operator");
		request.addParameter("meta_b_trunc", "trunc operator");
		request.addParameter("meta_c_orplus", "orplus operator");
		request.addParameter("meta_d_orsand", "orsand operator");
		request.addParameter("meta_e_or", "or operator");
		request.addParameter("meta_f_phrase", "phrase \"operator\"");
		request.addParameter("meta_g_prox", "prox operator");
		request.addParameter("meta_h_and", "and operator");
		request.addParameter("meta_I_sand", "sand operator");
		request.addParameter("meta_j_not", "not operator");
		request.addParameter("dummy", "value");
		
		MetaParameters processor = new MetaParameters();
		processor.process(st, request);
		
		Assert.assertEquals(10, st.getQuestion().getMetaParameters().size());
		Assert.assertEquals("a:simple a:operator", st.getQuestion().getMetaParameters().get(0));
		Assert.assertEquals("*b:trunc* *b:operator*", st.getQuestion().getMetaParameters().get(1));
		Assert.assertEquals("+[c:orplus c:operator]", st.getQuestion().getMetaParameters().get(2));
		Assert.assertEquals("|[d:orsand d:operator]", st.getQuestion().getMetaParameters().get(3));
		Assert.assertEquals("[e:or e:operator]", st.getQuestion().getMetaParameters().get(4));
		Assert.assertEquals("f:\"phrase operator\"", st.getQuestion().getMetaParameters().get(5));
		Assert.assertEquals("g:`prox operator`", st.getQuestion().getMetaParameters().get(6));
		Assert.assertEquals("+h:and +h:operator", st.getQuestion().getMetaParameters().get(7));
		Assert.assertEquals("|I:sand |I:operator", st.getQuestion().getMetaParameters().get(8));
		Assert.assertEquals("-j:not -j:operator", st.getQuestion().getMetaParameters().get(9));
	}
	
	@Test
	public void testMetaSingleWord() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_a", "simple");
		request.addParameter("meta_b_trunc", "batman");
		request.addParameter("meta_c_orplus", "spiderman");
		request.addParameter("meta_d_orsand", "ironman");
		request.addParameter("meta_e_or", "elephantman");
		request.addParameter("meta_f_phrase", "stallman");		// Isn't he a super hero ? :)
		request.addParameter("meta_g_prox", "superman");
		request.addParameter("meta_h_and", "hulk");				// Ran out of *man
		request.addParameter("meta_i_sand", "captainamerica");	// Ran out of single words
		request.addParameter("meta_j_not", "silversurfer");
		request.addParameter("dummy", "value");
		
		MetaParameters processor = new MetaParameters();
		processor.process(st, request);
		
		Assert.assertEquals(10, st.getQuestion().getMetaParameters().size());
		Assert.assertEquals("a:simple", st.getQuestion().getMetaParameters().get(0));
		Assert.assertEquals("*b:batman*", st.getQuestion().getMetaParameters().get(1));
		Assert.assertEquals("+[c:spiderman]", st.getQuestion().getMetaParameters().get(2));
		Assert.assertEquals("|[d:ironman]", st.getQuestion().getMetaParameters().get(3));
		Assert.assertEquals("[e:elephantman]", st.getQuestion().getMetaParameters().get(4));
		Assert.assertEquals("f:\"stallman\"", st.getQuestion().getMetaParameters().get(5));
		Assert.assertEquals("g:`superman`", st.getQuestion().getMetaParameters().get(6));
		Assert.assertEquals("+h:hulk", st.getQuestion().getMetaParameters().get(7));
		Assert.assertEquals("|i:captainamerica", st.getQuestion().getMetaParameters().get(8));
		Assert.assertEquals("-j:silversurfer", st.getQuestion().getMetaParameters().get(9));
	}
	
	@Test
	public void testQueryMultipleWords() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("query_trunc", "trunc operator");
		request.addParameter("query_orplus", "orplus operator");
		request.addParameter("query_orsand", "orsand operator");
		request.addParameter("query_or", "or operator");
		request.addParameter("query_phrase", "phrase operator");
		request.addParameter("query_prox", "prox operator");
		request.addParameter("query_and", "and operator");
		request.addParameter("query_sand", "sand operator");
		request.addParameter("query_not", "not operator");
		request.addParameter("dummy", "value");
		
		MetaParameters processor = new MetaParameters();
		processor.process(st, request);
		
		Assert.assertEquals(9, st.getQuestion().getMetaParameters().size());
		Assert.assertEquals("*trunc* *operator*", st.getQuestion().getMetaParameters().get(0));
		Assert.assertEquals("+[orplus operator]", st.getQuestion().getMetaParameters().get(1));
		Assert.assertEquals("|[orsand operator]", st.getQuestion().getMetaParameters().get(2));
		Assert.assertEquals("[or operator]", st.getQuestion().getMetaParameters().get(3));
		Assert.assertEquals("\"phrase operator\"", st.getQuestion().getMetaParameters().get(4));
		Assert.assertEquals("`prox operator`", st.getQuestion().getMetaParameters().get(5));
		Assert.assertEquals("+and +operator", st.getQuestion().getMetaParameters().get(6));
		Assert.assertEquals("|sand |operator", st.getQuestion().getMetaParameters().get(7));
		Assert.assertEquals("-not -operator", st.getQuestion().getMetaParameters().get(8));
	}

	@Test
	public void testQuerySingleWord() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("query_trunc", "batman");
		request.addParameter("query_orplus", "spiderman");
		request.addParameter("query_orsand", "ironman");
		request.addParameter("query_or", "elephantman");
		request.addParameter("query_phrase", "stallman");		// Isn't he a super hero ? :)
		request.addParameter("query_prox", "superman");
		request.addParameter("query_and", "hulk");				// Ran out of *man
		request.addParameter("query_sand", "captainamerica");	// Ran out of single words
		request.addParameter("query_not", "silversurfer");
		request.addParameter("dummy", "value");
		
		MetaParameters processor = new MetaParameters();
		processor.process(st, request);
		
		Assert.assertEquals(9, st.getQuestion().getMetaParameters().size());
		Assert.assertEquals("*batman*", st.getQuestion().getMetaParameters().get(0));
		Assert.assertEquals("+[spiderman]", st.getQuestion().getMetaParameters().get(1));
		Assert.assertEquals("|[ironman]", st.getQuestion().getMetaParameters().get(2));
		Assert.assertEquals("[elephantman]", st.getQuestion().getMetaParameters().get(3));
		Assert.assertEquals("\"stallman\"", st.getQuestion().getMetaParameters().get(4));
		Assert.assertEquals("`superman`", st.getQuestion().getMetaParameters().get(5));
		Assert.assertEquals("+hulk", st.getQuestion().getMetaParameters().get(6));
		Assert.assertEquals("|captainamerica", st.getQuestion().getMetaParameters().get(7));
		Assert.assertEquals("-silversurfer", st.getQuestion().getMetaParameters().get(8));
	}
	
	@Test
	public void testInvalidParameters() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_", "incomplete");
		request.addParameter("meta_x_invalid", "second bad");	
		request.addParameter("meta_x_", "incomplete too");
		request.addParameter("query_invalid", "first bad");
		request.addParameter("query_", "incomplete");
		request.addParameter("meta_invalid", "abc");
		request.addParameter("meta_inv_or", "def");
		
		MetaParameters processor = new MetaParameters();
		processor.process(st, request);
		
		Assert.assertEquals(7, st.getQuestion().getMetaParameters().size());
		Assert.assertEquals("incomplete", st.getQuestion().getMetaParameters().get(0));
		Assert.assertEquals("x:second x:bad", st.getQuestion().getMetaParameters().get(1));
		Assert.assertEquals("x:incomplete x:too", st.getQuestion().getMetaParameters().get(2));
		Assert.assertEquals("first bad", st.getQuestion().getMetaParameters().get(3));
		Assert.assertEquals("incomplete", st.getQuestion().getMetaParameters().get(4));
		Assert.assertEquals("abc", st.getQuestion().getMetaParameters().get(5));
		Assert.assertEquals("[def]", st.getQuestion().getMetaParameters().get(6));
	}
	
	@Test
	public void testCombination() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_Z_phrase_sand", "incomplete");
		
		MetaParameters processor = new MetaParameters();
		processor.process(st, request);
		
		Assert.assertEquals(1, st.getQuestion().getMetaParameters().size());
		Assert.assertEquals("|Z:\"incomplete\"", st.getQuestion().getMetaParameters().get(0));
	}
	
}

