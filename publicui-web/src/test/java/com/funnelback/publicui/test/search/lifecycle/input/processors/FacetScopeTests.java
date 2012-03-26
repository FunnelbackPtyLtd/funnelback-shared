package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.SneakyThrows;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetScope;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class FacetScopeTests {

	private SearchTransaction st;
	private FacetScope processor;
	
	@Before
	public void before() {
		processor = new FacetScope();
		st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
	}
	
	@Test
	public void testInvalidParameters() {
		try {
			processor.processInput(null);
			processor.processInput(new SearchTransaction(null, null));
			processor.processInput(new SearchTransaction(new SearchQuestion(), null));
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testNoFacetScope() throws InputProcessorException {
		Assert.assertEquals(0, st.getQuestion().getInputParameterMap().size());
		Assert.assertEquals(0, st.getQuestion().getRawInputParameters().size());
		
		processor.processInput(st);

		Assert.assertEquals(0, st.getQuestion().getInputParameterMap().size());
		Assert.assertEquals(0, st.getQuestion().getRawInputParameters().size());
	}
	
	@Test
	public void test() throws Exception {
		st.getQuestion().getInputParameterMap().put("facetScope",
				//encode(
						encode("f.Location|X") + "=" + encode("australia")
						+ "&" + encode("f.Type|1") + "=" + encode("part time")
						+ "&" + encode("f.Url|url") + "=" + encode("prospects & sales")	// With ampersand
						+ "&" + encode("f.Type|1") + "=" + encode("full time"));//);			// Second value for same param
		
		System.out.println(st.getQuestion().getInputParameterMap().get("facetScope"));

		processor.processInput(st);

		// 4 because "facetScope" is still in the Map
		Assert.assertEquals(4, st.getQuestion().getInputParameterMap().size());
		Assert.assertEquals(3, st.getQuestion().getRawInputParameters().size());
		
		for (String s: new String[] {"f.Location|X", "f.Type|1", "f.Url|url"}) {
			Assert.assertTrue(st.getQuestion().getInputParameterMap().keySet().contains(s));
			Assert.assertTrue(st.getQuestion().getRawInputParameters().keySet().contains(s));
		}
		
		Assert.assertEquals(st.getQuestion().getInputParameterMap().get("f.Location|X"), "australia");
		Assert.assertEquals(st.getQuestion().getInputParameterMap().get("f.Url|url"), "prospects & sales");
		Assert.assertTrue(st.getQuestion().getInputParameterMap().get("f.Type|1").equals("part time")
				|| st.getQuestion().getInputParameterMap().get("f.Type|1").equals("full time"));
		

		Assert.assertEquals(st.getQuestion().getRawInputParameters().get("f.Location|X")[0], "australia");
		Assert.assertEquals(st.getQuestion().getRawInputParameters().get("f.Url|url")[0], "prospects & sales");
		
		String[] s = st.getQuestion().getRawInputParameters().get("f.Type|1");
		Assert.assertEquals(2, s.length);
		Assert.assertTrue(s[0].equals("part time") || s[0].equals("full time"));
		Assert.assertTrue(s[1].equals("part time") || s[1].equals("full time"));
	}
	
	@SneakyThrows(UnsupportedEncodingException.class)
	private String encode(String s) {
		return URLEncoder.encode(s, "UTF-8");
	}

	
}
