package com.funnelback.publicui.test.search.web.controllers;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.web.controllers.ResourcesController;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class ResourcesControllerTest {

	@Autowired
	private ResourcesController controller;
	
	@Autowired
	private MockConfigRepository configRepository;

	private MockHttpServletRequest req;
	
	@Before
	public void before() {
		req = new MockHttpServletRequest();
		req.setMethod("GET");
		
		configRepository.removeAllCollections();
		Collection c = new Collection("resources-controller", new NoOptionsConfig("resources-controller"));
		c.getProfiles().put("_default", null);
		c.getProfiles().put("profile-folder", null);
		configRepository.addCollection(c);
	}
	
	@Test
	public void testInvalidCollection() throws Exception {
		MockHttpServletResponse resp = new MockHttpServletResponse();
		
		req.setRequestURI("/resources/invalid-collection/file.txt");
		controller.handleRequest("invalid-collection", req, resp);		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
		
		req.setRequestURI("/resources/");
		resp = new MockHttpServletResponse();
		controller.handleRequest("", req, resp);		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());

		req.setRequestURI("/resources/file.txt");
		resp = new MockHttpServletResponse();
		controller.handleRequest("file.txt", req, resp);		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
	}
	
	@Test
	public void testInvalidProfile() throws Exception {
		MockHttpServletResponse resp = new MockHttpServletResponse();
		
		req.setRequestURI("/resources/resources-controller/invalid-profile/file1.txt");
		controller.handleRequest("resources-controller", req, resp);		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
	}
	
	@Test
	public void testFileNotFound() throws Exception {
		MockHttpServletResponse resp = new MockHttpServletResponse();
		
		req.setRequestURI("/resources/resources-controller/file.txt");
		resp = new MockHttpServletResponse();
		controller.handleRequest("resources-controller", req, resp);		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());

		req.setRequestURI("/resources/resources-controller/_default/file.txt");
		resp = new MockHttpServletResponse();
		controller.handleRequest("resources-controller", req, resp);		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
	}
	
	/**
	 * Relies on Spring throwing an Exception because the test is not running
	 * in a WebApplicationContext but in a TestContext.
	 * @see https://jira.springsource.org/browse/SPR-5243
	 */
	@Test
	public void testFiles() throws Exception {
		MockHttpServletResponse resp = new MockHttpServletResponse();
		
		try {
			req.setRequestURI("/resources/resources-controller/file1.txt");
			resp = new MockHttpServletResponse();
			controller.handleRequest("resources-controller", req, resp);
			Assert.fail();
		} catch (IllegalStateException ise) { }

		try {
			req.setRequestURI("/resources/resources-controller/_default/file1.txt");
			resp = new MockHttpServletResponse();
			controller.handleRequest("resources-controller", req, resp);		
		Assert.fail();
		} catch (IllegalStateException ise) { }
		
		try {
			req.setRequestURI("/resources/resources-controller/profile-folder/file2.txt");
			resp = new MockHttpServletResponse();
			controller.handleRequest("resources-controller", req, resp);		
			Assert.fail();
		} catch (IllegalStateException ise) { }
	}

	/**
	 * Relies on Spring throwing an Exception because the test is not running
	 * in a WebApplicationContext but in a TestContext.
	 * @see https://jira.springsource.org/browse/SPR-5243
	 */
	@Test
	public void testSubFolders() throws Exception {
		MockHttpServletResponse resp = new MockHttpServletResponse();

		try {
			req.setRequestURI("/resources/resources-controller/sub-folder/sub-file1.txt");
			resp = new MockHttpServletResponse();
			controller.handleRequest("resources-controller", req, resp);		
			Assert.fail();
		} catch (IllegalStateException ise) { }

		try {
			req.setRequestURI("/resources/resources-controller/_default/sub-folder/sub-file1.txt");
			resp = new MockHttpServletResponse();
			controller.handleRequest("resources-controller", req, resp);		
			Assert.fail();
		} catch (IllegalStateException ise) { }

		try {
			req.setRequestURI("/resources/resources-controller/profile-folder/sub-folder/sub-file2.txt");
			resp = new MockHttpServletResponse();
			controller.handleRequest("resources-controller", req, resp);		
			Assert.fail();
		} catch (IllegalStateException ise) { }

	}
	
	@Test
	public void testInvalidPath() throws Exception {
		MockHttpServletResponse resp = new MockHttpServletResponse();
		
		req.setRequestURI("/resources/resources-controller/_default/../collection.cfg");
		controller.handleRequest("resources-controller", req, resp);		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
	}

}
