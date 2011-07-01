package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MetaParameters;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MetaParametersTests {
	
	@Test
	public void testMissingData() throws InputProcessorException {
		MetaParameters processor = new MetaParameters();
		
		// No transaction
		processor.processInput(null);
		
		// No question
		processor.processInput(new SearchTransaction(null, null));
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		processor.processInput(new SearchTransaction(question, null));		
	}
	
	@Test
	public void testNoMetaParameters() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		MetaParameters processor = new MetaParameters();
		processor.processInput(st);
		
		Assert.assertEquals(0, st.getQuestion().getMetaParameters().size());
	}
	
	@Test
	public void testNoParameterValue() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		st.getQuestion().getInputParameterMap().put("meta_a", null);
		st.getQuestion().getInputParameterMap().put("meta_c_or", "");
		st.getQuestion().getInputParameterMap().put("meta_X_and", null);
		st.getQuestion().getInputParameterMap().put("query_phrase", null);
		st.getQuestion().getInputParameterMap().put("query_and", "");
		st.getQuestion().getInputParameterMap().put("query_or", null);
		st.getQuestion().getInputParameterMap().put("unrelated", null);
		
		MetaParameters processor = new MetaParameters();
		processor.processInput(st);
		
		Assert.assertEquals(0, st.getQuestion().getMetaParameters().size());
	}
	
	@Test
	public void testMetaMultipleWords() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

		st.getQuestion().getInputParameterMap().put("meta_a", "simple operator");
		st.getQuestion().getInputParameterMap().put("meta_b_trunc", "trunc operator");
		st.getQuestion().getInputParameterMap().put("meta_c_orplus", "orplus operator");
		st.getQuestion().getInputParameterMap().put("meta_d_orsand", "orsand operator");
		st.getQuestion().getInputParameterMap().put("meta_e_or", "or operator");
		st.getQuestion().getInputParameterMap().put("meta_f_phrase", "phrase \"operator\"");
		st.getQuestion().getInputParameterMap().put("meta_g_prox", "prox operator");
		st.getQuestion().getInputParameterMap().put("meta_h_and", "and operator");
		st.getQuestion().getInputParameterMap().put("meta_I_sand", "sand operator");
		st.getQuestion().getInputParameterMap().put("meta_j_not", "not operator");
		st.getQuestion().getInputParameterMap().put("dummy", "value");

		MetaParameters processor = new MetaParameters();
		processor.processInput(st);
		
		Assert.assertEquals(10, st.getQuestion().getMetaParameters().size());
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("a:simple a:operator"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("*b:trunc* *b:operator*"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[c:orplus c:operator]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[d:orsand d:operator]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[e:or e:operator]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("f:\"phrase operator\""));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("g:`prox operator`"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+h:and +h:operator"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|I:sand |I:operator"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-j:not -j:operator"));
	}
	
	@Test
	public void testMetaSingleWord() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		st.getQuestion().getInputParameterMap().put("meta_a", "simple");
		st.getQuestion().getInputParameterMap().put("meta_b_trunc", "batman");
		st.getQuestion().getInputParameterMap().put("meta_c_orplus", "spiderman");
		st.getQuestion().getInputParameterMap().put("meta_d_orsand", "ironman");
		st.getQuestion().getInputParameterMap().put("meta_e_or", "elephantman");
		st.getQuestion().getInputParameterMap().put("meta_f_phrase", "stallman");		// Isn't he a super hero ? :)
		st.getQuestion().getInputParameterMap().put("meta_g_prox", "superman");
		st.getQuestion().getInputParameterMap().put("meta_h_and", "hulk");				// Ran out of *man
		st.getQuestion().getInputParameterMap().put("meta_i_sand", "captainamerica");	// Ran out of single words
		st.getQuestion().getInputParameterMap().put("meta_j_not", "silversurfer");
		st.getQuestion().getInputParameterMap().put("dummy", "value");
		
		MetaParameters processor = new MetaParameters();
		processor.processInput(st);
		
		Assert.assertEquals(10, st.getQuestion().getMetaParameters().size());
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("a:simple"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("*b:batman*"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[c:spiderman]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[d:ironman]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[e:elephantman]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("f:\"stallman\""));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("g:`superman`"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+h:hulk"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|i:captainamerica"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-j:silversurfer"));
	}
	
	@Test
	public void testQueryMultipleWords() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		st.getQuestion().getInputParameterMap().put("query_trunc", "trunc operator");
		st.getQuestion().getInputParameterMap().put("query_orplus", "orplus operator");
		st.getQuestion().getInputParameterMap().put("query_orsand", "orsand operator");
		st.getQuestion().getInputParameterMap().put("query_or", "or operator");
		st.getQuestion().getInputParameterMap().put("query_phrase", "phrase operator");
		st.getQuestion().getInputParameterMap().put("query_prox", "prox operator");
		st.getQuestion().getInputParameterMap().put("query_and", "and operator");
		st.getQuestion().getInputParameterMap().put("query_sand", "sand operator");
		st.getQuestion().getInputParameterMap().put("query_not", "not operator");
		st.getQuestion().getInputParameterMap().put("dummy", "value");
		
		MetaParameters processor = new MetaParameters();
		processor.processInput(st);
		
		Assert.assertEquals(9, st.getQuestion().getMetaParameters().size());
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("*trunc* *operator*"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[orplus operator]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[orsand operator]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[or operator]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("\"phrase operator\""));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("`prox operator`"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+and +operator"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|sand |operator"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-not -operator"));
	}

	@Test
	public void testQuerySingleWord() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		st.getQuestion().getInputParameterMap().put("query_trunc", "batman");
		st.getQuestion().getInputParameterMap().put("query_orplus", "spiderman");
		st.getQuestion().getInputParameterMap().put("query_orsand", "ironman");
		st.getQuestion().getInputParameterMap().put("query_or", "elephantman");
		st.getQuestion().getInputParameterMap().put("query_phrase", "stallman");		// Isn't he a super hero ? :)
		st.getQuestion().getInputParameterMap().put("query_prox", "superman");
		st.getQuestion().getInputParameterMap().put("query_and", "hulk");				// Ran out of *man
		st.getQuestion().getInputParameterMap().put("query_sand", "captainamerica");	// Ran out of single words
		st.getQuestion().getInputParameterMap().put("query_not", "silversurfer");
		st.getQuestion().getInputParameterMap().put("dummy", "value");
		
		MetaParameters processor = new MetaParameters();
		processor.processInput(st);
		
		Assert.assertEquals(9, st.getQuestion().getMetaParameters().size());
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("*batman*"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[spiderman]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[ironman]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[elephantman]"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("\"stallman\""));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("`superman`"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+hulk"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|captainamerica"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-silversurfer"));
	}
	
	@Test
	public void testInvalidParameters() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

		st.getQuestion().getInputParameterMap().put("meta_", "incomplete");
		st.getQuestion().getInputParameterMap().put("meta_x_invalid", "second bad");	
		st.getQuestion().getInputParameterMap().put("meta_x_", "incomplete too");
		st.getQuestion().getInputParameterMap().put("query_invalid", "first bad");
		st.getQuestion().getInputParameterMap().put("query_", "incomplete");
		st.getQuestion().getInputParameterMap().put("meta_invalid", "abc");
		st.getQuestion().getInputParameterMap().put("meta_inv_or", "def");
		
		MetaParameters processor = new MetaParameters();
		processor.processInput(st);
		
		Assert.assertEquals(7, st.getQuestion().getMetaParameters().size());
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("incomplete"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("x:second x:bad"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("x:incomplete x:too"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("first bad"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("incomplete"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("abc"));
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[def]"));
	}
	
	@Test
	public void testCombination() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

		st.getQuestion().getInputParameterMap().put("meta_Z_phrase_sand", "incomplete");
		
		MetaParameters processor = new MetaParameters();
		processor.processInput(st);
		
		Assert.assertEquals(1, st.getQuestion().getMetaParameters().size());
		Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|Z:\"incomplete\""));
	}
	
	@Test
	public void testMetaDatesAreSkipped() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		st.getQuestion().getInputParameterMap().put("meta_d1day", "1day");
		st.getQuestion().getInputParameterMap().put("meta_d2month", "2month");
		st.getQuestion().getInputParameterMap().put("meta_d3year", "3year");
		st.getQuestion().getInputParameterMap().put("meta_wday", "wday");
		st.getQuestion().getInputParameterMap().put("meta_xmonth", "xmonth");
		st.getQuestion().getInputParameterMap().put("meta_yyear", "yyear");
		
		new MetaParameters().processInput(st);
		
		Assert.assertEquals(0, st.getQuestion().getMetaParameters().size());
	}
	
}

