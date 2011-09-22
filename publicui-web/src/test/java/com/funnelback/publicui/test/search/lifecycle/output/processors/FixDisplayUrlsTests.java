package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FixDisplayUrls;
import com.funnelback.publicui.search.model.padre.ResultPacket;

public class FixDisplayUrlsTests extends FixPseudoLiveLinksTests {

	@Autowired
	protected FixDisplayUrls processor;
	
	@Test
	public void test() throws UnsupportedEncodingException, OutputProcessorException {
		processor.processOutput(st);
		
		ResultPacket rp = st.getResponse().getResultPacket();
		
		Assert.assertEquals(
				"local://serve-db-document.tcgi?collection=collection-db&record_id=1234/",
				rp.getResults().get(0).getDisplayUrl());
		
		Assert.assertEquals(
				"local://serve-connector-document.tcgi?collection=collection-connector&primaryAttribute=1234",
				rp.getResults().get(1).getDisplayUrl());

		Assert.assertEquals(
				"trim://45/356/",
				rp.getResults().get(2).getDisplayUrl());
		
		Assert.assertEquals(
				"\\\\server.funnelback.com\\share\\folder\\file.ext",
				rp.getResults().get(3).getDisplayUrl());

		Assert.assertEquals(
				"C:\\folder\\file.ext",
				rp.getResults().get(4).getDisplayUrl());

		Assert.assertEquals(
				"/folder/file.ext",
				rp.getResults().get(5).getDisplayUrl());

		Assert.assertEquals(
				"Unknown collection URL should be left inchanged",
				"http://www.result.com",
				rp.getResults().get(6).getDisplayUrl());

		Assert.assertEquals(
				"Web collection should have unchanged URL",
				"http://www.site.com/folder/result.html",
				rp.getResults().get(7).getDisplayUrl());

		Assert.assertEquals(
				"DB result without local:// prefix should be left unchanged",
				"custom-show-db-record.cgi?collection=collection-db&record_id=1234/",
				rp.getResults().get(8).getDisplayUrl());

		Assert.assertEquals(
				"TRIM result without trim:// prefix should be left unchanged",
				"http://trim-ws.company.com/show-record?uri=356",
				rp.getResults().get(9).getDisplayUrl());	
		
		Assert.assertEquals(
				"trim://45/1234/",
				rp.getResults().get(10).getDisplayUrl());
		
	}
	
}
