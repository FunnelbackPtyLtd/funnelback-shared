package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.PadreForking;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.xml.XmlParsingException;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinNT;

public class PadreForkingTests {

	private PadreForking forking;
	
	@Before
	public void before() {
		forking = new PadreForking();
		forking.setPadreWaitTimeout(30);
		forking.setPadreXmlParser(new StaxStreamParser());
		forking.setSearchHome(new File("src/test/resources/dummy-search_home"));

	}
	
	@Test
	public void test() throws DataFetchException, EnvironmentVariableException, IOException {
		String ext = ".sh";
		if (OS.isFamilyWindows()) {
			ext = ".bat";
		}
		
		String qp = "mock-padre"+ext+" src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml";
		
		SearchQuestion qs = new SearchQuestion();
		qs.setCollection(new Collection("padre-forking", new NoOptionsConfig("padre-forking").setValue("query_processor", qp)));
		qs.setQuery("test");
		SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
		
		
		forking.fetchData(ts);
		
		Assert.assertNotNull(ts.getResponse());
		Assert.assertEquals(FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml")), ts.getResponse().getRawPacket());
		Assert.assertEquals(10, ts.getResponse().getResultPacket().getResults().size());
		Assert.assertEquals("Online visa applications", ts.getResponse().getResultPacket().getResults().get(0).getTitle());
	}
	
	@Test
	public void testInvalidPacket() throws Exception {
		String ext = ".sh";
		if (OS.isFamilyWindows()) {
			ext = ".bat";
		}
		
		String qp = "mock-padre"+ext+" src/test/resources/dummy-search_home/conf/padre-forking/mock-packet-invalid.xml.bad";
		
		SearchQuestion qs = new SearchQuestion();
		qs.setCollection(new Collection("padre-forking", new NoOptionsConfig("padre-forking").setValue("query_processor", qp)));
		qs.setQuery("test");
		SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
		
		
		try {
			forking.fetchData(ts);
			Assert.fail();
		} catch (DataFetchException dfe) {
			Assert.assertEquals(XmlParsingException.class, dfe.getCause().getClass());
		}
	}
	
	@Test
	public void testInvalidQueryProcessor() throws FileNotFoundException, EnvironmentVariableException {
		SearchQuestion qs = new SearchQuestion();
		qs.setCollection(new Collection("padre-forking", new NoOptionsConfig("padre-forking").setValue("query_processor", "invalid")));
		qs.setQuery("test");
		SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
		
		
		try {
			forking.fetchData(ts);
			Assert.fail();
		} catch (DataFetchException dfe) {
			Assert.assertEquals(PadreForkingException.class, dfe.getCause().getClass());
		}
	}
	
	@Test
	public void testErrorReturn() throws Exception {
		String ext = ".sh";
		if (OS.isFamilyWindows()) {
			ext = ".bat";
		}
		
		String qp = "mock-padre-error"+ext;

		SearchQuestion qs = new SearchQuestion();
		qs.setCollection(new Collection("padre-forking", new NoOptionsConfig("padre-forking").setValue("query_processor", qp)));
		qs.setQuery("test");
		SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
		
		
		forking.fetchData(ts);
		
		Assert.assertEquals(68, ts.getResponse().getReturnCode());
	}
	
	@Test
	public void testWindowsNative() throws Exception {
		Assume.assumeTrue(OS.isFamilyWindows());
		
		String ext = ".sh";
		if (OS.isFamilyWindows()) {
			ext = ".bat";
		}
		
		String qp = "mock-padre"+ext+" src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml";
		
		SearchQuestion qs = new SearchQuestion();
		qs.setCollection(new Collection("padre-forking", new NoOptionsConfig("padre-forking").setValue("query_processor", qp)));
		qs.setQuery("test");
		qs.setImpersonated(true);
		SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
		
		Advapi32.INSTANCE.ImpersonateSelf(WinNT.SECURITY_IMPERSONATION_LEVEL.SecurityImpersonation);
		forking.fetchData(ts);
		Advapi32.INSTANCE.RevertToSelf();
		
		Assert.assertNotNull(ts.getResponse());
		Assert.assertEquals(FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml")), ts.getResponse().getRawPacket());
		Assert.assertEquals(10, ts.getResponse().getResultPacket().getResults().size());
		Assert.assertEquals("Online visa applications", ts.getResponse().getResultPacket().getResults().get(0).getTitle());
	}
	
}
