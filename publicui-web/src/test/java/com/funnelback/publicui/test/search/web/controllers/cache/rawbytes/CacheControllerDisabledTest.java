package com.funnelback.publicui.test.search.web.controllers.cache.rawbytes;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.controllers.CacheController;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class CacheControllerDisabledTest {

	protected CacheController cacheController;
	
	@Resource(name="localConfigRepository")
	protected ConfigRepository configRepository;
	
	@Resource(name="localDataRepository")
	protected DataRepository dataRepository;

	protected MockHttpServletRequest request;
	protected MockHttpServletResponse response;

	@Before
	public void before() throws IOException {
		cacheController = new CacheController();
		cacheController.setConfigRepository(configRepository);
		cacheController.setDataRepository(dataRepository);
		
		request = new MockHttpServletRequest();
		request.setRequestURI("/s/cache.html");
		response = new MockHttpServletResponse();
		
		// Make sure all conditions are met for cache to be enabled
		Config config = configRepository.getCollection("cache-disabled").getConfiguration();
		config.setValue(Keys.UI_CACHE_DISABLED, "false");
		config.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, null);
		config.setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, null);
	}

	@Test
	public void testCacheDisabled() throws Exception {
		configRepository.getCollection("cache-disabled").getConfiguration().setValue(Keys.UI_CACHE_DISABLED, "true");
		ModelAndView mav = cacheController.cache(request,
				response,
				configRepository.getCollection("cache-disabled"),
				DefaultValues.PREVIEW_SUFFIX,
				DefaultValues.DEFAULT_FORM,
				"unused");
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals(CacheController.CACHED_COPY_UNAVAILABLE_VIEW, mav.getViewName());
	}

}
