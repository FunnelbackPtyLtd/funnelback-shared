package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

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
			processor.process(null, null);
			processor.process(new SearchTransaction(null, null), null);
			processor.process(new SearchTransaction(new SearchQuestion(), null), null);
			Assert.assertEquals(0, st.getQuestion().getQueryExpressions().size());
			
			processor.process(new SearchTransaction(new SearchQuestion(), null), new MockHttpServletRequest());
			Assert.assertEquals(0, st.getQuestion().getQueryExpressions().size());
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testNoDateParameters() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("param1", "value1");
		request.addParameter("meta_x", "y");
		
		processor.process(st, request);
		Assert.assertNull(st.getQuestion().getQuery());
		Assert.assertEquals(0, st.getQuestion().getQueryExpressions().size());
		
		st.getQuestion().setQuery("existing query");
		processor.process(st, request);
		Assert.assertEquals("existing query", st.getQuestion().getQuery());
		Assert.assertEquals(0, st.getQuestion().getQueryExpressions().size());
	}
	
	@Test
	public void testQueryPreserved() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_d", "01Jan2010");
		
		st.getQuestion().setQuery("existing query");
		processor.process(st, request);
		Assert.assertEquals("existing query", st.getQuestion().getQuery());
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d=01Jan2010", st.getQuestion().getQueryExpressions().get(0));
	}
	
	@Test
	public void testDateOnThreeComponents() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_dyear", "1965");
		request.addParameter("meta_dmonth", "Jan");
		request.addParameter("meta_dday", "6");
		
		processor.process(st, request);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d=6Jan1965", st.getQuestion().getQueryExpressions().get(0));
		st.getQuestion().getQueryExpressions().clear();
		
		request.setParameter("meta_dday", (String) null);
		processor.process(st, request);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d=Jan1965", st.getQuestion().getQueryExpressions().get(0));
		st.getQuestion().getQueryExpressions().clear();
		
		request = new MockHttpServletRequest();
		request.addParameter("meta_d3year", "2011");
		request.addParameter("meta_d3month", "03");
		request.addParameter("meta_d3day", "06");
		
		processor.process(st, request);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>5Mar2011", st.getQuestion().getQueryExpressions().get(0));
		
	}
	
	@Test
	public void testD() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_d", "01Jan2001");

		processor.process(st, request);	
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d=01Jan2001", st.getQuestion().getQueryExpressions().get(0));
	}
	
	@Test
	public void testD1D2() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_d1", "01Jan2001");
		request.addParameter("meta_d2", "05Mar2002");

		processor.process(st, request);
		Assert.assertEquals(2, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>01Jan2001", st.getQuestion().getQueryExpressions().get(0));
		Assert.assertEquals("d<05Mar2002", st.getQuestion().getQueryExpressions().get(1));
	}
	
	@Test
	public void testD3D4() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_d3", "01Jan2001");
		request.addParameter("meta_d4", "05Mar2002");

		processor.process(st, request);
		Assert.assertEquals(2, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>31Dec2000", st.getQuestion().getQueryExpressions().get(0));
		Assert.assertEquals("d<6Mar2002", st.getQuestion().getQueryExpressions().get(1));
		st.getQuestion().getQueryExpressions().clear();
		
		request.setParameter("meta_d3", "2010-05-01");
		request.setParameter("meta_d4", "2011-06-06");

		processor.process(st, request);
		Assert.assertEquals(2, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>30Apr2010", st.getQuestion().getQueryExpressions().get(0));
		Assert.assertEquals("d<7Jun2011", st.getQuestion().getQueryExpressions().get(1));
		st.getQuestion().getQueryExpressions().clear();
		
		request.setParameter("meta_d4", "invalid date");
		processor.process(st, request);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("d>30Apr2010", st.getQuestion().getQueryExpressions().get(0));
	}
	
	@Test
	public void testEventSearch() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_w1", "01Jan2001");
		request.addParameter("meta_w2", "05Mar2002");

		processor.process(st, request);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("% w>01Jan2001<05Mar2002", st.getQuestion().getQueryExpressions().get(0));
		st.getQuestion().getQueryExpressions().clear();
		
		// Existing query + w2
		request.setParameter("meta_w1", (String) null);
		st.getQuestion().setQuery("existing query");
		processor.process(st, request);
		Assert.assertEquals("existing query", st.getQuestion().getQuery());
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("% w<05Mar2002", st.getQuestion().getQueryExpressions().get(0));
		st.getQuestion().getQueryExpressions().clear();
		
		// w1 only
		request = new MockHttpServletRequest();
		request.addParameter("meta_w1", "2002-01");
		st.getQuestion().setQuery(null);
		processor.process(st, request);
		Assert.assertEquals(1, st.getQuestion().getQueryExpressions().size());
		Assert.assertEquals("% w>2002-01", st.getQuestion().getQueryExpressions().get(0));

	}
	
}