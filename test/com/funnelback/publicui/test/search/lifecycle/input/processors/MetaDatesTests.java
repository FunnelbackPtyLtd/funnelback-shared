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
			processor.process(new SearchTransaction(new SearchQuestion(), null), new MockHttpServletRequest());
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
		
		st.getQuestion().setQuery("existing query");
		processor.process(st, request);
		Assert.assertEquals("existing query", st.getQuestion().getQuery());
	}
	
	@Test
	public void testQueryPreserved() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_d", "01Jan2010");
		
		st.getQuestion().setQuery("existing query");
		processor.process(st, request);
		Assert.assertEquals("existing query d=01Jan2010", st.getQuestion().getQuery());		
	}
	
	@Test
	public void testDateOnThreeComponents() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_dyear", "1965");
		request.addParameter("meta_dmonth", "Jan");
		request.addParameter("meta_dday", "6");
		
		processor.process(st, request);
		Assert.assertEquals("d=6Jan1965", st.getQuestion().getQuery());
		
		request.setParameter("meta_dday", (String) null);
		st.getQuestion().setQuery(null);
		processor.process(st, request);
		Assert.assertEquals("d=Jan1965", st.getQuestion().getQuery());
		
		request = new MockHttpServletRequest();
		request.addParameter("meta_d3year", "2011");
		request.addParameter("meta_d3month", "03");
		request.addParameter("meta_d3day", "06");
		
		st.getQuestion().setQuery(null);
		processor.process(st, request);
		Assert.assertEquals("d>5Mar2011", st.getQuestion().getQuery());
	}
	
	@Test
	public void testD() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_d", "01Jan2001");

		processor.process(st, request);	
		Assert.assertEquals("d=01Jan2001", st.getQuestion().getQuery());
	}
	
	@Test
	public void testD1D2() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_d1", "01Jan2001");
		request.addParameter("meta_d2", "05Mar2002");

		processor.process(st, request);
		Assert.assertEquals("d>01Jan2001 d<05Mar2002", st.getQuestion().getQuery());
	}
	
	@Test
	public void testD3D4() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_d3", "01Jan2001");
		request.addParameter("meta_d4", "05Mar2002");

		processor.process(st, request);
		Assert.assertEquals("d>31Dec2000 d<6Mar2002", st.getQuestion().getQuery());

		request.setParameter("meta_d3", "2010-05-01");
		request.setParameter("meta_d4", "2011-06-06");
		st.getQuestion().setQuery(null);

		processor.process(st, request);
		Assert.assertEquals("d>30Apr2010 d<7Jun2011", st.getQuestion().getQuery());
		
		request.setParameter("meta_d4", "invalid date");
		st.getQuestion().setQuery(null);
		processor.process(st, request);
		Assert.assertEquals("d>30Apr2010", st.getQuestion().getQuery());
	}
	
	@Test
	public void testEventSearch() throws InputProcessorException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("meta_w1", "01Jan2001");
		request.addParameter("meta_w2", "05Mar2002");

		processor.process(st, request);
		Assert.assertEquals("% w>01Jan2001<05Mar2002", st.getQuestion().getQuery());
		
		// Existing query + w2
		request.setParameter("meta_w1", (String) null);
		st.getQuestion().setQuery("existing query");
		processor.process(st, request);
		Assert.assertEquals("existing query % w<05Mar2002", st.getQuestion().getQuery());
		
		// w1 only
		request = new MockHttpServletRequest();
		request.addParameter("meta_w1", "2002-01");
		st.getQuestion().setQuery(null);
		processor.process(st, request);
		Assert.assertEquals("% w>2002-01", st.getQuestion().getQuery());

	}
	
}