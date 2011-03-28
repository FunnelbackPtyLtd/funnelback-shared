package com.funnelback.publicui.test.web.filters;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.publicui.search.model.collection.paramtransform.ParamTransformRuleFactory;
import com.funnelback.publicui.search.web.filters.RequestParametersTransformWrapper;

public class RequestParametersTransformWrapperTests {

	@Test
	public void testMissingParams() {
		try {
			new RequestParametersTransformWrapper(null, null);
			Assert.fail("null origin request should throw " + IllegalArgumentException.class.getSimpleName());
		} catch (IllegalArgumentException iae) {}
		
		
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("param1", "value1");
		
		RequestParametersTransformWrapper wrapper = new RequestParametersTransformWrapper(req, null);
		Assert.assertEquals(1, wrapper.getParameterMap().size());
		Assert.assertEquals("value1", wrapper.getParameter("param1"));

		wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(null));
		Assert.assertEquals(1, wrapper.getParameterMap().size());
		Assert.assertEquals("value1", wrapper.getParameter("param1"));
		
		wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(new String[0]));
		Assert.assertEquals(1, wrapper.getParameterMap().size());
		Assert.assertEquals("value1", wrapper.getParameter("param1"));

	}
	
	@Test
	public void testInvalidRules() {
		String[] rules = {
			" => inserted_param=value",
			" param1 => ",
			" param1 =>",
			"this is not a rule",
			" =value => missing=name",
			" param=value => =missing"
		};
		
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("param1", "value1");
		req.addParameter("extra", "identical");
		
		RequestParametersTransformWrapper wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules));
		Assert.assertEquals(2, wrapper.getParameterMap().size());
		Assert.assertEquals("value1", wrapper.getParameter("param1"));
		Assert.assertEquals("identical", wrapper.getParameter("extra"));
		Assert.assertNull(wrapper.getParameter("invalid"));
	}
	
	@Test
	public void testInsert() {
		String[] rules = {
			"param1=value1 => inserted_paramA=valueA&inserted_paramB=valueB&inserted_paramB=valueC&novalue=",
			"param1=value2 => shouldnt_be=inserted"
		};
			
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("param1", "value1");
		req.addParameter("extra", "identical");
		
		RequestParametersTransformWrapper wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules));
		Assert.assertEquals(5, wrapper.getParameterMap().size());
		Assert.assertEquals("value1", wrapper.getParameter("param1"));
		Assert.assertEquals("valueA", wrapper.getParameter("inserted_paramA"));
		Assert.assertArrayEquals(new String[] {"valueB", "valueC"}, wrapper.getParameterValues("inserted_paramB"));
		Assert.assertArrayEquals(new String[0], wrapper.getParameterValues("novalue"));
		Assert.assertEquals("identical", wrapper.getParameter("extra"));
	}
	
	@Test
	public void testAddExisting() {
		String[] rules = {
				"param1=value1 => param1=newValue1",
			};
				
			MockHttpServletRequest req = new MockHttpServletRequest();
			req.addParameter("param1", "value1");
			req.addParameter("extra", "identical");
			
			RequestParametersTransformWrapper wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules));
			Assert.assertEquals(2, wrapper.getParameterMap().size());
			Assert.assertArrayEquals(new String[] {"value1", "newValue1"}, wrapper.getParameterValues("param1"));
			Assert.assertEquals("identical", wrapper.getParameter("extra"));
	}
	
	@Test
	public void testRemove() {
		String[] rules = {
				"param1=value1 => -scope",
				"param1=value1 => -profile=default",
				"param2=value2 => -profile=test",
				"param2=value2 => -nonexistent=ok"
		};
		
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("param1", "value1");
		req.addParameter("scope", new String[] {"scope1", "scope2"});
		req.addParameter("extra", "identical");
		req.addParameter("profile", new String[] {"default", "test", "plop"});

		RequestParametersTransformWrapper wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules));
		Assert.assertEquals(3, wrapper.getParameterMap().size());
		Assert.assertEquals("value1", wrapper.getParameter("param1"));
		Assert.assertEquals("identical", wrapper.getParameter("extra"));
		Assert.assertArrayEquals(new String[] {"test", "plop"}, wrapper.getParameterValues("profile"));
		
		req.addParameter("param2", "value2");

		wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules));
		Assert.assertEquals(4, wrapper.getParameterMap().size());
		Assert.assertEquals("value1", wrapper.getParameter("param1"));
		Assert.assertEquals("identical", wrapper.getParameter("extra"));
		Assert.assertArrayEquals(new String[] {"plop"}, wrapper.getParameterValues("profile"));
	}
	
	@Test
	public void testMultipleRules() {
		String[] rules = {
				"coverage=abcnews => -scope",
				"coverage=abcnews => profile=news&clive=abc&clive=news&scope=/news"
		};
		
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("query", "sharks");
		req.addParameter("coverage", "abcnews");
		req.addParameter("scope", "/");

		RequestParametersTransformWrapper wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules));
		Assert.assertEquals(5, wrapper.getParameterMap().size());
		Assert.assertArrayEquals(new String[] {"sharks"}, wrapper.getParameterValues("query"));
		Assert.assertArrayEquals(new String[] {"abcnews"}, wrapper.getParameterValues("coverage"));
		Assert.assertArrayEquals(new String[] {"news"}, wrapper.getParameterValues("profile"));
		Assert.assertEquals("news", wrapper.getParameter("profile"));
		Assert.assertArrayEquals(new String[] {"abc", "news"}, wrapper.getParameterValues("clive"));
		Assert.assertArrayEquals(new String[] {"/news"}, wrapper.getParameterValues("scope"));

	}
	
	@Test
	public void testBroadLeftSide() {
		String[] rules = {
			"param1 => -scope",
			"param2= => -profile",
			"param3 => xml=1"
		};
		
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("query", "sharks");
		req.addParameter("collection", "business-gov");
		req.addParameter("profile", "agencies");
		req.addParameter("scope", new String[] {"/news", "/local"});
		req.addParameter("param1", (String) null);
		req.addParameter("param2", "");
		req.addParameter("param3", "anything");
		
		RequestParametersTransformWrapper wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules));
		Assert.assertEquals(6, wrapper.getParameterMap().size());
		Assert.assertArrayEquals(new String[] {"sharks"}, wrapper.getParameterValues("query"));
		Assert.assertArrayEquals(new String[] {"business-gov"}, wrapper.getParameterValues("collection"));
		Assert.assertArrayEquals(new String[] {"1"}, wrapper.getParameterValues("xml"));
		Assert.assertArrayEquals(new String[] {null}, wrapper.getParameterValues("param1"));
		Assert.assertArrayEquals(new String[] {""}, wrapper.getParameterValues("param2"));
		Assert.assertArrayEquals(new String[] {"anything"}, wrapper.getParameterValues("param3"));
	}
	
	/**
	 * Test cases found on Nitrogen
	 */
	@Test
	public void realTestCases() {

		// Rules #1
		String[] rules1 = { "collection=business-gov => collection=business-gov&extra_state-gov_gscope1=9,10,11,12,13,14,15,16" };
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("query", "sharks");
		req.addParameter("collection", "business-gov");

		RequestParametersTransformWrapper wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules1));
		Assert.assertEquals(3, wrapper.getParameterMap().size());
		Assert.assertArrayEquals(new String[] {"sharks"}, wrapper.getParameterValues("query"));
		Assert.assertArrayEquals(new String[] {"business-gov", "business-gov"}, wrapper.getParameterValues("collection"));
		Assert.assertArrayEquals(new String[] {"9,10,11,12,13,14,15,16"}, wrapper.getParameterValues("extra_state-gov_gscope1"));
		
		// Rules #2
		String[] rules2 = { "location= => -extra_local-council_disable" };
		req = new MockHttpServletRequest();
		req.addParameter("query", "sharks");
		req.addParameter("collection", "business-gov");
		req.addParameter("location", "anything");
		req.addParameter("extra_local-council_disable", "on");
		
		wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules2));
		Assert.assertEquals(3, wrapper.getParameterMap().size());
		Assert.assertArrayEquals(new String[] {"sharks"}, wrapper.getParameterValues("query"));
		Assert.assertArrayEquals(new String[] {"business-gov"}, wrapper.getParameterValues("collection"));
		Assert.assertArrayEquals(new String[] {"anything"}, wrapper.getParameterValues("location"));
		Assert.assertNull(wrapper.getParameterValues("extra_local-council_disable"));
		
		// Rules #3
		String[] rules3 = { "location=0832 => extra_state-gov_gscope1=16&extra_local-council_gscope1=19&extra_local-council_num_ranks=3" };
		req = new MockHttpServletRequest();
		req.addParameter("query", "sharks");
		req.addParameter("collection", "business-gov");
		req.addParameter("location", "0832");
		req.addParameter("extra_local-council_disable", "on");
		
		wrapper = new RequestParametersTransformWrapper(req, ParamTransformRuleFactory.buildRules(rules3));
		Assert.assertEquals(7, wrapper.getParameterMap().size());
		Assert.assertArrayEquals(new String[] {"sharks"}, wrapper.getParameterValues("query"));
		Assert.assertArrayEquals(new String[] {"business-gov"}, wrapper.getParameterValues("collection"));
		Assert.assertArrayEquals(new String[] {"0832"}, wrapper.getParameterValues("location"));
		Assert.assertArrayEquals(new String[] {"on"}, wrapper.getParameterValues("extra_local-council_disable"));
		Assert.assertArrayEquals(new String[] {"16"}, wrapper.getParameterValues("extra_state-gov_gscope1"));
		Assert.assertArrayEquals(new String[] {"19"}, wrapper.getParameterValues("extra_local-council_gscope1"));
		Assert.assertArrayEquals(new String[] {"3"}, wrapper.getParameterValues("extra_local-council_num_ranks"));
		
	}
	
}


