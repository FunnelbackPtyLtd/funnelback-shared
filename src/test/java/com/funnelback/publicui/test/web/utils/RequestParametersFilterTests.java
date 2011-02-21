package com.funnelback.publicui.test.web.utils;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.publicui.web.utils.RequestParametersFilter;

public class RequestParametersFilterTests {

	@Test
	public void test() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("param1", "");
		request.addParameter("param2", "");
		request.addParameter("meta_X", "y");
		request.addParameter("meta_X_or", "y");
		
		RequestParametersFilter f = new RequestParametersFilter(request);
		
		String[] actual = f.filter("^(p|m).*");
		Assert.assertEquals(4, actual.length);
		Assert.assertEquals("param1", actual[0]);
		Assert.assertEquals("param2", actual[1]);
		Assert.assertEquals("meta_X", actual[2]);
		Assert.assertEquals("meta_X_or", actual[3]);
		
		Assert.assertEquals(1, f.filter(".*X$").length);
		Assert.assertEquals("meta_X", f.filter(".*X$")[0]);
		
		Assert.assertEquals(0, f.filter("test").length);
	}
	
}
