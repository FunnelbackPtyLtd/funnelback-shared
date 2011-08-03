package com.funnelback.contentoptimiser.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.funnelback.common.config.Keys;
import com.funnelback.common.utils.cgirunner.CgiRunner;
import com.funnelback.common.utils.cgirunner.CgiRunnerException;
import com.funnelback.contentoptimiser.UrlStatus;
import com.funnelback.contentoptimiser.fetchers.UrlStatusFetcher;
import com.funnelback.contentoptimiser.fetchers.impl.DefaultUrlStatusFetcher;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.utils.CgiRunnerFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
public class DefaultUrlStatusFetcherTest {

	UrlStatusFetcher fetcher;
	private CgiRunner cgiRunner;
	
	
	@Before 
	public void setup() throws CgiRunnerException {
		File searchHome = new File("src/test/resources/dummy-search_home");
		ConfigRepository configRepository = mock(ConfigRepository.class);
		cgiRunner = mock(CgiRunner.class);	
		CgiRunnerFactory factory = mock(CgiRunnerFactory.class);
		
		when(configRepository.getExecutablePath(Keys.Executables.PERL)).thenReturn("perl");
		when(factory.create(new File(searchHome, "web" + File.separator + "admin" + File.separator+ "url-status.cgi"), new File("perl"))).thenReturn(cgiRunner);
		when(cgiRunner.addRequestParameter(Mockito.anyString(), Mockito.anyString())).thenReturn(cgiRunner);
		when(cgiRunner.setTaint()).thenReturn(cgiRunner);
		when(cgiRunner.setEnvironmentVariable(Mockito.anyString(), Mockito.anyString())).thenReturn(cgiRunner);
		when(cgiRunner.runToString()).thenReturn("Expires: Wed, 03 Aug 2011 05:42:27 GMT\n" +
				"Date: Wed, 03 Aug 2011 05:37:27 GMT\n" +
				"Content-Type: application/json\n" +
				"\n" +
				"{\"error\":\"Unsupported collection type: local\",\"message\":\"<p> URL status information not available for collection type: local\"}\"");
		
		
		DefaultUrlStatusFetcher fetcher = new DefaultUrlStatusFetcher();
		fetcher.setCgiRunnerFactory(factory);
		fetcher.setConfigRepository(configRepository);
		fetcher.setSearchHome(searchHome);
		this.fetcher = fetcher;
	}
	
	@Test
	public void testNoHttp() {
		fetcher.fetch("www.place.com", "dummy-collection");
		verify(cgiRunner).addRequestParameter("c", "dummy-collection");
		verify(cgiRunner).addRequestParameter("u", "http://www.place.com");
		verify(cgiRunner).addRequestParameter("f", "json");
		verify(cgiRunner).setTaint();
		verify(cgiRunner).setEnvironmentVariable("REMOTE_USER", "admin");
		verify(cgiRunner).setEnvironmentVariable("SEARCH_HOME", new File("src/test/resources/dummy-search_home").getAbsolutePath());
	}
	
	@Test
	public void testHttp() {
		fetcher.fetch("http://www.place.com", "dummy-collection");
		verify(cgiRunner).addRequestParameter("c", "dummy-collection");
		verify(cgiRunner).addRequestParameter("u", "http://www.place.com");
		verify(cgiRunner).addRequestParameter("f", "json");
		verify(cgiRunner).setTaint();
		verify(cgiRunner).setEnvironmentVariable("REMOTE_USER", "admin");
		verify(cgiRunner).setEnvironmentVariable("SEARCH_HOME", new File("src/test/resources/dummy-search_home").getAbsolutePath());
	}
	
	@Test
	public void testHttps() {
		fetcher.fetch("https://www.place.com", "dummy-collection");
		verify(cgiRunner).addRequestParameter("c", "dummy-collection");
		verify(cgiRunner).addRequestParameter("u", "https://www.place.com");
		verify(cgiRunner).addRequestParameter("f", "json");
		verify(cgiRunner).setTaint();
		verify(cgiRunner).setEnvironmentVariable("REMOTE_USER", "admin");
		verify(cgiRunner).setEnvironmentVariable("SEARCH_HOME", new File("src/test/resources/dummy-search_home").getAbsolutePath());
	}
	
	@Test
	public void testJson() {
		UrlStatus u = fetcher.fetch("https://www.place.com", "dummy-collection");
		Assert.assertEquals(null, u.getAvailable());
		Assert.assertEquals("Unsupported collection type: local", u.getError());
	}
	
}

