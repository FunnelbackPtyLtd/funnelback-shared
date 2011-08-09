package com.funnelback.publicui.test.search.web.views.freemarker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.search.web.views.freemarker.CustomisableFreeMarkerFormView;

public class CustomisableFreeMarkerFormViewTest extends CustomisableFreeMarkerFormView {

	private Config config;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	
	@Before
	public void before() throws Exception {
		config =new NoOptionsConfig("dummy");
		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(new Collection("dummy", config));
		
		response = new MockHttpServletResponse();
		response.setContentType("text/html");
		
		request = new MockHttpServletRequest();
		request.setAttribute(SearchController.ModelAttributes.question.toString(), sq);		
	}
	
	@Test
	public void testNothingToDo() {
		// No custom content type or header
		setCustomHeaders("simple", config, response);
		setCustomContentType("simple", config, response);
		
		Assert.assertEquals("text/html", response.getContentType());
		Assert.assertEquals(0, response.getHeaderNames().size());
	}
	
	@Test
	public void testCustomContentType() {
		config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX, "test/junit");
		setCustomHeaders("simple", config, response);
		setCustomContentType("simple", config, response);

		Assert.assertEquals("test/junit", response.getContentType());
		Assert.assertEquals(0, response.getHeaderNames().size());
	}
	
	@Test
	public void testCustomHeaders() {
		config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_COUNT_SUFFIX, "2");
		config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_SUFFIX + ".1", "First-Header: Value 1   ");
		config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_SUFFIX + ".2", "Second-Header     :second value...");
		// The third one should be ignored
		config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_SUFFIX + ".3", "Third-Header: value 3");
		
		setCustomHeaders("simple", config, response);
		setCustomContentType("simple", config, response);

		Assert.assertEquals("text/html", response.getContentType());
		Assert.assertEquals(2, response.getHeaderNames().size());
		Assert.assertEquals("Value 1", response.getHeader("First-Header"));
		Assert.assertEquals("second value...", response.getHeader("Second-Header"));
	}

	@Test
	public void testBoth() {
		config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX, "text/csv");
		config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_COUNT_SUFFIX, "1");
		config.setValue(Keys.ModernUI.FORM_PREFIX + ".simple." + Keys.ModernUI.HEADERS_SUFFIX + ".1", "Content-Disposition: attachment");

		setCustomHeaders("simple", config, response);
		setCustomContentType("simple", config, response);

		Assert.assertEquals("text/csv", response.getContentType());
		Assert.assertEquals(1, response.getHeaderNames().size());
		Assert.assertEquals("attachment", response.getHeader("Content-Disposition"));
		
	}

	
}
