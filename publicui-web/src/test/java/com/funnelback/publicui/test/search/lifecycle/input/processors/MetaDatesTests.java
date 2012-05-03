package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MetaDates;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MetaDatesTests {

	private MetaDates processor = new MetaDates();
	private SearchTransaction st;
	
	@Before
	public void before() {
		processor = new MetaDates();
		st = new SearchTransaction(new SearchQuestion(), null);
	}
	

	@Test
	public void testInvalidParameters() {
		try {
			processor.processInput(null);
			processor.processInput(new SearchTransaction(null, null));
			processor.processInput(new SearchTransaction(new SearchQuestion(), null));
			Assert.assertEquals(0, st.getQuestion().getQueryExpressions().size());
			
			processor.processInput(new SearchTransaction(new SearchQuestion(), null));
			Assert.assertEquals(0, st.getQuestion().getQueryExpressions().size());
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testNoDateParameters() throws InputProcessorException {
		
		st.getQuestion().getRawInputParameters().put("param1", new String[] {"value1"});
		st.getQuestion().getRawInputParameters().put("meta_x", new String[] {"y"});
		
		processor.processInput(st);
		Assert.assertNull(st.getQuestion().getQuery());
		Assert.assertEquals(0, st.getQuestion().getQueryExpressions().size());
		
		st.getQuestion().setQuery("existing query");
		processor.processInput(st);
		Assert.assertEquals("existing query", st.getQuestion().getQuery());
		Assert.assertEquals(0, st.getQuestion().getQueryExpressions().size());
	}
	
	@Test
	public void testQueryPreserved() throws InputProcessorException {
		st.getQuestion().getRawInputParameters().put("meta_d", new String[] {"01Jan2010"});
		
		st.getQuestion().setQuery("existing query");
		processor.processInput(st);
		Assert.assertEquals("existing query", st.getQuestion().getQuery());
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d=01Jan2010", st.getQuestion().getQueryExpressions().get(0));
	}
	
	@Test
	public void testDateOnThreeComponents() throws InputProcessorException {

		st.getQuestion().getRawInputParameters().put("meta_dyear", new String[] {"1965"});
		st.getQuestion().getRawInputParameters().put("meta_dmonth", new String[] {"Jan"});
		st.getQuestion().getRawInputParameters().put("meta_dday", new String[] {"6"});
		
		processor.processInput(st);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d=6Jan1965", st.getQuestion().getQueryExpressions().get(0));
		st.getQuestion().getQueryExpressions().clear();
		
		st.getQuestion().getRawInputParameters().put("meta_dday", null);
		processor.processInput(st);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d=Jan1965", st.getQuestion().getQueryExpressions().get(0));
		st.getQuestion().getQueryExpressions().clear();
		
		st.getQuestion().getRawInputParameters().clear();
		st.getQuestion().getRawInputParameters().put("meta_d3year", new String[] {"2011"});
		st.getQuestion().getRawInputParameters().put("meta_d3month", new String[] {"03"});
		st.getQuestion().getRawInputParameters().put("meta_d3day", new String[] {"06"});
		
		processor.processInput(st);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>5Mar2011", st.getQuestion().getQueryExpressions().get(0));
		
	}
	
	@Test
	public void testD() throws InputProcessorException {
		st.getQuestion().getRawInputParameters().put("meta_d", new String[] {"01Jan2001"});

		processor.processInput(st);	
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d=01Jan2001", st.getQuestion().getQueryExpressions().get(0));
	}
	
	@Test
	public void testD1D2() throws InputProcessorException {
		st.getQuestion().getRawInputParameters().put("meta_d1", new String[] {"01Jan2001"});
		st.getQuestion().getRawInputParameters().put("meta_d2", new String[] {"05Mar2002"});

		processor.processInput(st);
		Assert.assertEquals(2, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>01Jan2001", st.getQuestion().getQueryExpressions().get(0));
		Assert.assertEquals("d<05Mar2002", st.getQuestion().getQueryExpressions().get(1));
	}
	
	@Test
	public void testD3D4() throws InputProcessorException {
		st.getQuestion().getRawInputParameters().put("meta_d3", new String[] {"01Jan2001"});
		st.getQuestion().getRawInputParameters().put("meta_d4", new String[] {"05Mar2002"});

		processor.processInput(st);
		Assert.assertEquals(2, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>31Dec2000", st.getQuestion().getQueryExpressions().get(0));
		Assert.assertEquals("d<6Mar2002", st.getQuestion().getQueryExpressions().get(1));
		st.getQuestion().getQueryExpressions().clear();
		
		st.getQuestion().getRawInputParameters().put("meta_d3", new String[] {"2010-05-01"});
		st.getQuestion().getRawInputParameters().put("meta_d4", new String[] {"2011-06-06"});

		processor.processInput(st);
		Assert.assertEquals(2, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>30Apr2010", st.getQuestion().getQueryExpressions().get(0));
		Assert.assertEquals("d<7Jun2011", st.getQuestion().getQueryExpressions().get(1));
		st.getQuestion().getQueryExpressions().clear();
		
		st.getQuestion().getRawInputParameters().put("meta_d4", new String[] {"invalid date"});
		processor.processInput(st);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>30Apr2010", st.getQuestion().getQueryExpressions().get(0));
	}
	
	@Test
	public void testEventSearch() throws InputProcessorException {

		st.getQuestion().getRawInputParameters().put("meta_w1", new String[] {"01Jan2001"});
		st.getQuestion().getRawInputParameters().put("meta_w2", new String[] {"05Mar2002"});

		processor.processInput(st);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("% w>01Jan2001<05Mar2002", st.getQuestion().getQueryExpressions().get(0));
		st.getQuestion().getQueryExpressions().clear();
		
		// Existing query + w2
		st.getQuestion().getRawInputParameters().put("meta_w1", null);
		st.getQuestion().setQuery("existing query");
		processor.processInput(st);
		Assert.assertEquals("existing query", st.getQuestion().getQuery());
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("% w<05Mar2002", st.getQuestion().getQueryExpressions().get(0));
		st.getQuestion().getQueryExpressions().clear();
		
		// w1 only
		st.getQuestion().getRawInputParameters().clear();
		st.getQuestion().getRawInputParameters().put("meta_w1", new String[] {"2002-01"});
		st.getQuestion().setQuery(null);
		processor.processInput(st);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("% w>2002-01", st.getQuestion().getQueryExpressions().get(0));

	}
	
}